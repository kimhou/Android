/**
 * 
 */

/**
 * @author kim
 *
 */
//DEFINE System.out.println println;

public class DataType {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		{
		System.out.println("-----基本数值类型-----");
		//变量, 常量
		final double PI = 3.141596;//常量
		int R = 10;//变量
		System.out.println("圆面积=" + PI*R*R);
		
		//整型
		byte byte1 = 127; //-2^7 ~ 2^7-1
		short int1 = 16; //-2^15 ~ 2^15-1
		int int2 = 32; //-2^31 ~ 2^31-1
		long int3 = 64L; //-2^63 ~ 2^63-1
		
		//浮点
		float f1 = 2.11f;
		double d1 = 3.232d;
		
		//字符
		char a = 'A';
		char b = '\u003a';
		}
		
		
		{
		System.out.println("-----基本运算-----");
		int i1 = 10;
		int i2 = 23;
		System.out.println("i1=" + i1);
		System.out.println("i2=" + i2);
		System.out.println("i1 + i2 = " + (i1 + i2));
		System.out.println("i1 * i2 = " + i1 * i2);
		System.out.println("i1/i2 = " + i1/i2);
		System.out.println("i1%i2 = " + i1%i2);
		
		String str1 = "string1";
		String str2 = "string2";
		System.out.println("string1=" + str1);
		System.out.println("string2=" + str2);
		System.out.println("string1 + string2 = " + str1+str2);
		
		byte b1 = 127;
		byte b2 = 5;
		System.out.println("b1="+b1);
		System.out.println("b2="+b2);
		System.out.println("b1+1=" + (b1+1));
		System.out.println("2^7=" + (2^7));
		}
		
		{
		//进制
		int ii1 = 10;
		int ii2 = 010;
		int ii3 = 0x1a;
		System.out.println("i1=" + ii1);
		System.out.println("i2=" + ii2);
		System.out.println("i3=" + ii3);
		}
		
		{
		//类型转换
		//自动类型转换只能低位数类型转高位数类型,否则编译报错,除非强转
		short s = 10;
		int int1 = s;
		long l = 323232l;
		int int2 = (int)l;
		double d = 10.223;
		int int3 = (int)d;
		System.out.println("自动类型转换, int1 = " + int1 + ", int2 = " + int2 + ", int3 = " + int3);

		}
		
	}

}
