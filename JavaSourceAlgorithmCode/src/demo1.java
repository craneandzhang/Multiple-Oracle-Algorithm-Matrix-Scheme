import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.util.Arrays;

public class demo1 {
    private static final int N = 1024; // 向量的大小定为N
    private static long t1 = 0;
    private static long t2 = 0;
    private static final Pairing pairing = PairingFactory.getPairing("D:\\java-code\\javaSE\\SomeAlgo\\src\\a.properties");
    public static void main(String[] args) {
        // 测试JPBC库
        // 生成一个随机数对
//        Element C = pairing.getZr().newRandomElement().getImmutable();
        Element W = pairing.getZr().newRandomElement().getImmutable();
        Element[] G_vector = new Element[N];
        for (int i = 0; i < N; i++) {
            G_vector[i] = pairing.getZr().newRandomElement().getImmutable();
        }
        Element[] a_vector = new Element[N];
        for (int i = 0; i < N; i++) {
            a_vector[i] = pairing.getZr().newRandomElement().getImmutable();
        }
//        Element[] b_vector = new Element[N];
//        for (int i = 0; i < N; i++) {
//            b_vector[i] = pairing.getZr().newRandomElement().getImmutable();
//        }
        Element[] b_vector = mapToBinaryVector();
        Element C = innerProduct(a_vector, b_vector).mul(W).add(innerProduct(a_vector, G_vector));
        long start = System.currentTimeMillis();
        boolean b = !algorithm1(G_vector, a_vector, b_vector, C, W, N);// 调用算法1()
        long end = System.currentTimeMillis();
        long l = end - start;
        System.out.println("算法1()总时间：" + l + "微秒");
        t1 = l - t2;
        System.out.println("算法1()运行时间：" + t1 + "微秒");
        System.out.println("算法1()的结果：" + b);
//        for (int i = 0; i < N; i++) {
//            System.out.print(a_vector[i] + " ");
//        }
//        System.out.println();
//        for (int i = 0; i < N; i++) {
//            System.out.print(b_vector[i] + " ");
//        }
    }
    public static boolean algorithm1(Element[] G_vector, Element[] a_vector, Element[] b_vector, Element C, Element W, int n) {
        if(n == 1){
            long start1 = System.currentTimeMillis();
            Element t = innerProduct(a_vector, b_vector);
            Element tmp = t.mul(G_vector[0]).add(a_vector[0].mul(G_vector[0]));
            boolean ans = C.equals(tmp);
            long end1 = System.currentTimeMillis();
            t2 = end1 - start1;
            System.out.println("算法1()的验证时间：" + t2 + "微秒");
            return ans;
        }else {
            int mid = n / 2;
            // 创建 GL 和 GR
            Element[] GL = Arrays.copyOfRange(G_vector, 0, mid); // 左半部分
            Element[] GR = Arrays.copyOfRange(G_vector, mid, G_vector.length); // 右半部分
            // 创建 aL 和 aR
            Element[] aL = Arrays.copyOfRange(a_vector, 0, mid); // 左半部分
            Element[] aR = Arrays.copyOfRange(a_vector, mid, a_vector.length); // 右半部分
            // 创建 bL 和 bR
            Element[] bL = Arrays.copyOfRange(b_vector, 0, mid); // 左半部分
            Element[] bR = Arrays.copyOfRange(b_vector, mid, b_vector.length); // 右半部分
            Element L = innerProduct(aL, bR).mul(W).add(innerProduct(aL, GR));
            Element R = innerProduct(aR, bL).mul(W).add(innerProduct(aR, GL));
            Element t = getRandomElementFromZpStar(pairing);
            Element tInverse = t.invert(); // t的逆元素
            Element[] a_vector_new = new Element[mid];
            for (int i = 0; i < mid; i++) {
                a_vector_new[i] = aL[i].add(tInverse.mul(aR[i]));
            }
            Element[] b_vector_new = new Element[mid];
            for (int i = 0; i < mid; i++) {
                b_vector_new[i] = bL[i].add(tInverse.mul(bR[i]));
            }
            Element C_mew = t.mul(L).add(C).add(tInverse.mul(R));
            Element[] G_vector_new = new Element[mid];
            for (int i = 0; i < mid; i++) {
                G_vector_new[i] = GL[i].mul(GR[i]);
            }
            return algorithm1(G_vector_new, a_vector_new, b_vector_new, C_mew, W, mid);
        }
    }
    public static Element innerProduct(Element[] a, Element[] b) {
        Element result = pairing.getZr().newZeroElement(); // 初始化结果为零元素
        // 计算内积
        for (int i = 0; i < a.length; i++) {
            Element temp = a[i].mul(b[i]); // 计算 a[i] 和 b[i] 的乘积
            result = result.add(temp); // 累加到结果
        }
        return result;
    }
    public static Element getRandomElementFromZpStar(Pairing pairing) {
        Element t;
        do {
            t = pairing.getZr().newRandomElement().getImmutable(); // 随机生成 Zr 中的元素
        } while (t.isZero()); // 确保 t 不是零元素
        return t;
    }
    public static Element[] mapToBinaryVector() {
        // 创建长度为 n 的向量
        Element[] binaryVector = new Element[N];
        // 将 n 的二进制表示填充到向量中
        for (int i = 0; i < N; i++) {
            if (i < Integer.SIZE) { // 只在有效范围内填充二进制位
                int bit = (N >> i) & 1; // 获取第 i 位的二进制位
                binaryVector[i] = pairing.getZr().newElementFromBytes(new byte[]{(byte) bit}).getImmutable();
            } else {
                // 其余元素填充为0
                binaryVector[i] = pairing.getZr().newZeroElement(); // 填充0
            }
        }
        return binaryVector;
    }
    // 翻转数组
    public static Element[] flipArray(Element[] array) {
        int length = array.length;
        Element[] flippedArray = new Element[length];
        for (int i = 0; i < length; i++) {
            flippedArray[i] = array[length - 1 - i];
        }
        return flippedArray;
    }
}
