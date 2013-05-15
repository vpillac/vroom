/*
* Log of changes:
* 2011.
*/
package longsVsDoubles;

import java.util.Random;

import dataGenerators.DoubleGenerator;
import dataGenerators.IntegerGenerator;

/**
 * @author Jorge E. Mendoza (jorge.mendoza@uco.f)
 * @since 2011.12.12
 * @args[0] number of nodes in the instance
 * @args[1] number of operations to perform
 */
public class Test2 {
	
	
	public static void main(String[] args){
		Random rnd=new Random(1);
		double[][] distanceMatrix=DoubleGenerator.getDoubleMatrix(Integer.valueOf(args[0]),Integer.valueOf(args[0]),rnd);
		int [][] pairs=IntegerGenerator.getIntMatrix(Integer.valueOf(args[1]), 2, rnd, Integer.valueOf(args[0]));
		DoublesVsLongs test=new DoublesVsLongs(distanceMatrix);
		test.testComparison(pairs);
	}	
}
