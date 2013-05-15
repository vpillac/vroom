using System;
using System.Collections.Generic;
using System.Linq;
using System.IO;
using System.Globalization;
using System.Threading;
namespace MapPointDist
{
    static class Program
    {
        static void Main(string[] args)
        {
            if (args.Length < 3 || args.Length > 4)
            {
                Console.Error.WriteLine("Usage: MapPointDist coordFile outputFile symmetric [numThreads]");
            }
            else
            {
                int numThreads = args.Length == 4 ? Convert.ToInt32(args[3]) : 1;
                bool symmetric = Convert.ToBoolean(args[2]);
                DistEvaluator prog = new DistEvaluator(args[0], args[1], symmetric,numThreads);
                prog.Run();
            }
        }
    }

    public class DistEvaluator
    {
        private int mThreadCount;

        private bool mSymmetric;
        public bool Symmetric() { return mSymmetric; }

        private string mCoordFile;
        private string mOutputFile;
        private double[,] mCoordinates;
        private double[,] mDistances ;
        private double[,] mTimes ;
        private int mSize;

        private int mNextI;
        private int mNextJ;

        private double mTotal ;
        private double mProgress = 0;
        private int mNextStep = 0;

        private int[] mNodeId;

        public DistEvaluator(string coordFile, string outputFile, bool symmetric, int threadCount)
        {
            this.mCoordFile = coordFile;
            this.mOutputFile = outputFile;
            this.mThreadCount = threadCount;
            this.mSymmetric = symmetric;
        }

        public void Run()
        {
            Console.WriteLine("Evaluation of distances and travel times using coordinates file " + this.mCoordFile);
            System.DateTime debut = DateTime.Now;

            //Read the coordinate file
            ReadCoordinates();
            mDistances= new double[mSize, mSize];
            mTimes  = new double[mSize, mSize];


            mTotal = mSymmetric?mSize * (mSize - 1) / 2:mSize*mSize;
            mProgress = 0;
            mNextStep = 0;

            try
            {
                //if (mThreadCount == 1)
                //    calculerDistancesSeq();
                //else
                EvaluateDistances();
            }
            catch (Exception e)
            {
                Console.Error.WriteLine(e.Message);
            }


            Console.Out.WriteLine("Checking that the distance matrix satisfies the triangular inequality");
            FixMatrix(mDistances);
            Console.Out.WriteLine("Checking that the time matrix satisfies the triangular inequality");
            FixMatrix(mTimes);

            WriteOutputFile();

            System.DateTime fin = DateTime.Now;


            Console.Out.WriteLine("Finished - Total time: " + (fin - debut));
        }

        private void EvaluateDistances()
        {
            mNextI = 0;
            mNextJ = 1;

            MapPointCall[] mapCalls = new MapPointCall[mThreadCount];
            for (int t = 0; t < mThreadCount; t++)
            {
                mapCalls[t] = new MapPointCall(this);
                Thread thread = new Thread(new ThreadStart(mapCalls[t].Run));
                thread.Name = "MapCallThread-"+t;
                thread.Start();
            }

            bool running = true;
            while (running)
            {
                running = false;
                foreach (MapPointCall c in mapCalls)
                {
                    running |= c.mRunning;
                }
            }
        }

        private System.Object lockThis = new System.Object();

        public void PrintProgress()
        {
            lock (lockThis)
            {
                mProgress += 1 / mTotal;

                if (mProgress > mNextStep / 100d)
                {
                    Console.Write(mNextStep + "%...");
                    if (mNextStep == 100)
                        Console.WriteLine();
                    mNextStep += 10;
                }
            }
        }

        public int[] NextIJ()
        {
            lock (lockThis)
            {

                int[] next;
                if (mNextI >= 0)
                {
                    next = new int[] { mNextI, mNextJ };
                    mNextJ++;
                    if (mNextJ == mNextI) mNextJ++;
                    if (mNextJ >= mSize)
                    {
                        mNextI++;
                        if (mNextI >= (Symmetric()?mSize - 1:mSize))
                        {
                            mNextI = -1;
                            mNextJ = -1;
                        }
                        else
                        {
                            mNextJ = Symmetric()?mNextI + 1:0;
                        }
                    }
                }
                else
                {
                    next = null;
                }
                return next;
            }
        }

        public double GetLat(int i)
        {
            return mCoordinates[i, 0];
        }

        public double GetLon(int i)
        {
            return mCoordinates[i, 1];
        }

        public bool IsSameLocation(int i, int j) {
            return Math.Abs(mCoordinates[i, 0] - mCoordinates[j, 0]) < 1e-6 && Math.Abs(mCoordinates[i, 1] - mCoordinates[j, 1]) < 1e-6;
        }

        public bool IsDistTimeAlreadyCalculared(int i, int j)
        {
            return this.mDistances[i, j] > 0;
        }

        public void SaveDistTime(int i, int j, double dist, double time)
        {
            lock (lockThis)
            {
                SaveDistTimeInternal(i, j, dist, time);

                //Save the distance and time for all the pairs (ii,jj) that have the same location as (i,j)
                for (int ii = i; ii < mSize; ii++)
                {
                    if (mNodeId[ii] == i)
                    {
                        for (int jj = j; jj < mSize; jj++)
                        {
                            if (mNodeId[jj] == j)
                            {
                                SaveDistTimeInternal(ii, jj, dist, time);
                            }
                        }
                    }
                }
            }
        }

        private void SaveDistTimeInternal(int i, int j, double dist, double time)
        {
            this.mDistances[i, j] = dist;
            this.mTimes[i, j] = time;

            if (Symmetric())
            {
                this.mDistances[j, i] = dist;
                this.mTimes[j, i] = time;
            }
            
        }

        private void WriteOutputFile()
        {
            // Output file
            using (StreamWriter output = new StreamWriter(this.mOutputFile))
            {
                // Force dot as decimal separator
                NumberFormatInfo nfi = new CultureInfo("en-US", false).NumberFormat;
                //INFO Section
                output.WriteLine(this.mSize+";"+this.mSymmetric);
                // Distance section
                output.WriteLine("DISTANCES");
                WriteMatrix(mDistances, output, nfi);
                output.WriteLine("TRAVEL TIMES");
                WriteMatrix(mTimes, output, nfi);

                output.Flush();
                output.Close();
            }
        }

        private void WriteMatrix(double[,] matrix, StreamWriter output, NumberFormatInfo nfi)
        {
            for (int i = 0; i < this.mSize; i++)
            {
                output.Write(i);
                for (int j = Symmetric()?i + 1:0; j < this.mSize; j++)
                {
                    output.Write(';' + matrix[i, j].ToString(nfi));
                }
                output.WriteLine();
            }
        }


        private void FixMatrix(double[,] matrix)
        {
            //Check that the distances/times verify triangular inequality
            bool allOk = false;

            while (!allOk)
            {
                allOk = true;
                for (int i = 0; i < mSize; i++)
                {
                    for (int j = 0; j < mSize; j++)
                    {
                        for (int k = 0; k < mSize; k++)
                        {
                            if (matrix[i, k] + matrix[k, j] < matrix[i, j])
                            {
                                matrix[i, j] = matrix[i, k] + matrix[k, j];
                                allOk = false;
                            }
                        }
                    }
                }
            }

        }

        private void ReadCoordinates()
        {
            Console.WriteLine("Reading coordinates");

            mSize = -1;
            StreamReader sr = new StreamReader(this.mCoordFile);
            // Force dot as decimal separator
            NumberFormatInfo nfi = new CultureInfo("en-US", false).NumberFormat;
            while (sr.ReadLine() != null)
                mSize++;
            sr.Close();
            Console.WriteLine(" Instance size: " + mSize);

            sr = new StreamReader(this.mCoordFile);

            mCoordinates = new double[mSize, 2];

            string line = sr.ReadLine(); // Skip the first line (header)
            while ((line = sr.ReadLine()) != null)
            {
                string[] stringvalues = line.Split(';');
                int i = Convert.ToInt32(stringvalues[0]);
                mCoordinates[i, 0] = Convert.ToDouble(stringvalues[1], nfi);
                mCoordinates[i, 1] = Convert.ToDouble(stringvalues[2], nfi);
            }

            sr.Close();

            //Check for duplicates
            mNodeId = new int[mSize];
            for (int i = 0; i < mSize; i++) {
                mNodeId[i] = i;
                for (int j = 0; j < i; j++) {
                    if (IsSameLocation(i,j)) { 
                        //We consider that the two locations are the same
                        mNodeId[j] = i;
                        break;
                    }
                }
            }

            Console.WriteLine("Coordinates successfully read");
                    }
    }

    public class MapPointCall
    {
        private MapPoint.ApplicationClass mMapPoint;
        private DistEvaluator mDistEvaluator;
        private MapPoint.Map mMap;
        private MapPoint.Route mRoute;
        public bool mRunning;

        public MapPointCall(DistEvaluator distEvaluator)
        {
            this.mDistEvaluator = distEvaluator;
            this.mMapPoint = new MapPoint.ApplicationClass();
            this.mMapPoint.Application.Units = MapPoint.GeoUnits.geoKm;
            this.mMapPoint.Visible = false;
            this.mMapPoint.UserControl = true;

            //Configure l'application
            mMap = this.mMapPoint.ActiveMap;
            mRoute = mMap.ActiveRoute;

            mMap.Parent.PaneState = MapPoint.GeoPaneState.geoPaneRoutePlanner;
        }

        public void Run()
        {
            mRunning = true;
            int[] next = null;
            while( ( next = mDistEvaluator.NextIJ())!=null){
                int i = next[0];
                int j = next[1];
                mRoute.Clear();
                mRoute.Waypoints.Add(mMap.GetLocation(mDistEvaluator.GetLat(i), mDistEvaluator.GetLon(i)), "" + i);
                mRoute.Waypoints.Add(mMap.GetLocation(mDistEvaluator.GetLat(j), mDistEvaluator.GetLon(j)), "" + j);
                mRoute.Calculate();

                mDistEvaluator.SaveDistTime(i, j, mRoute.Distance, mRoute.TripTime / MapPoint.GeoTimeConstants.geoOneMinute);

                mDistEvaluator.PrintProgress();
            }

            mRoute.Clear();
            mMap.Saved = true;
            mMapPoint.Quit();
            mRunning = false;
        }
    }


}
