package radiocommsystem;
public class Std {

	public static String setWidth(String string, int length) {
		return String.format("%1$" + length + "s", string);
	}

	public static void o(String str) {
		System.out.print(str);
	}

	public static void o(String format, Object... args) {
		System.out.printf(format, args);
	}

	public static void out(String str) {
		System.out.println(str);
	}

	public static void out() {
		System.out.println();
	}

	public static void out(String format, Object... args) {
		System.out.printf(format, args);
		out();
	}
}
