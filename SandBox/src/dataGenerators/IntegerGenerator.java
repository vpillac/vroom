/*
* Log of changes:
* 2011.
*/
package dataGenerators;

import java.util.Random;

/**
 * Implements a generator of data structures filled up with integer types
 * @author Jorge E. Mendoza (jorge.mendoza@uco.f)
 * @since 2011.12.12
 */
public class IntegerGenerator {
	
	
	/**
	 * 
	 * @param rows the number of rows in the matrix
	 * @param columns the number of columns in the matrix
	 * @param rnd the random number generator
	 * @return a [rows][columns] matrix of randomly generated doubles
	 */
	public static int[][] getIntMatrix(int rows,int columns, Random rnd, int maxValue){
		int[][] matrix=new int[rows][columns];		
		for(int i=0;i<rows;i++){
			for(int j=0;j<columns;j++){
				matrix[i][j]=rnd.nextInt(maxValue);
			}
		}		
		return matrix;
	}
	
}
