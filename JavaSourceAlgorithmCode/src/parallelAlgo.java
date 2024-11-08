import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class parallelAlgo {
    private static final int N = 1024; // 向量的大小定为N
    private static long t1 = 0;
    private static long t2 = 0;
    private static final Pairing pairing = PairingFactory.getPairing("D:\\java-code\\javaSE\\SomeAlgo\\src\\a.properties");
    private static final int M  = 10;
    public static void main(String[] args) {
        // 并行计算
        Element O = pairing.getZr().newZeroElement();
        Element W = pairing.getZr().newZeroElement();
        Element G = pairing.getZr().newZeroElement();
        Element[] b_vector = mapToBinaryVector();
        // 一个矩阵m*n
        Element[][] a_matrix = new Element[M][N];
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                a_matrix[i][j] = pairing.getZr().newRandomElement().getImmutable();
            }
        }
        Element[] C_group = new Element[M];
        for (int i = 0; i < M; i++) {
            C_group[i] = innerProduct(a_matrix[i], b_vector).mul(W);
        }
        long start1 = System.currentTimeMillis();
        boolean b = algorithm3(C_group, a_matrix, b_vector, W, N);
        long end1 = System.currentTimeMillis();
        long l = end1 - start1;
        System.out.println("算法3()总时间：" + l + "微秒");
        t1 = l - t2;
        System.out.println("算法3()运行时间：" + t1 + "微秒");
        System.out.println(b);
    }

    // 并行计算算法
    public static boolean algorithm3(Element[] C_group, Element[][] a_matrix, Element[] b_vector, Element W, int n) {
        Element[] R_group = new Element[M];
        Element[] r_group = new Element[M];
        Element[] u_group = new Element[M];
        Element[] z_group = new Element[M];
        Element[] x_group = new Element[M];
        Boolean b = true;
        long start2 = System.currentTimeMillis();
        for (int i = 0; i < M; i++) {
            r_group[i] = pairing.getZr().newRandomElement().getImmutable();
            R_group[i] = r_group[i].mul(W);
            u_group[i] = pairing.getZr().newRandomElement().getImmutable();
            x_group[i] = pairing.getZr().newRandomElement().getImmutable();
            z_group[i] = r_group[i].add(u_group[i].mul(innerProduct(a_matrix[i], b_vector)));
            Element left = z_group[i].mul(W);
            Element right = R_group[i].add(u_group[i].mul(C_group[i]));
            if(!left.equals(right)){
                b = false;
                break;
            }
        }
        long end2 = System.currentTimeMillis();
        t2 = end2 - start2;
        System.out.println("算法3()的计算时间：" + t2 + "毫秒");
        return b;
    }

    // 计算内积
    public static Element innerProduct(Element[] a, Element[] b) {
        Element result = pairing.getZr().newZeroElement(); // 初始化结果为零元素
        // 计算内积
        for (int i = 0; i < a.length; i++) {
            Element temp = a[i].mul(b[i]); // 计算 a[i] 和 b[i] 的乘积
            result = result.add(temp); // 累加到结果
        }
        return result;
    }

    // 生成随机元素
    public static Element getRandomElementFromZpStar(Pairing pairing) {
        Element t;
        do {
            t = pairing.getZr().newRandomElement().getImmutable(); // 随机生成 Zr 中的元素
        } while (t.isZero()); // 确保 t 不是零元素
        return t;
    }

    // 将 n 映射到二进制向量
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
}
