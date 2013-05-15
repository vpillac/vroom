package vroom.sandbox;

public class SandBox {

    static Integer[][] memTestInteger(int size) {
        Integer[] ints = new Integer[size];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = Integer.valueOf(i);
        }

        Integer[][] matrix = new Integer[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = ints[j];
            }
        }
        return matrix;
    }

    static int[][] memTestInt(int size) {
        int[][] matrix = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = j;
            }
        }
        return matrix;
    }

    public static void main(String[] args) {
        // int size = 10000;
        //
        // int mb = 1024 * 1024;
        // memTestInteger(size);
        // long memInteger = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
        // / mb;
        // System.out.println(memInteger);
        // long memInt = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / mb;
        // System.out.println(memInt);
        // memTestInt(size);
        // long memInt = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / mb
        // - memInteger;
        // System.out.printf("Integer: %smb int:%smb", memInteger, memInt);

        // Integer[] test = new Integer[] { 1, 10, 2, 3, 4, 5, 6, 7, 8, 9 };
        // Arrays.sort(test, new Comparator<Integer>() {
        //
        // @Override
        // public int compare(Integer o1, Integer o2) {
        // return Integer.compare(o1, o2);
        // }
        // });
        // System.out.println(Utilities.toShortString(test));

    }
}
