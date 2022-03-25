package radiocommsystem;

public class V {
	public enum StaticLoudness { // V1.
		VeryQuiet, Quiet, Moderate, Loud, VeryLoud;

		public static StaticLoudness o1(int loudness) { // o1: A1 -> V(.)1 A1 = {-50, -49, -48, ..., 0} dB
			if (loudness >= -50 && loudness <= -41)
				return VeryQuiet;
			else if (loudness >= -40 && loudness <= -31)
				return Quiet;
			else if (loudness >= -30 && loudness <= -21)
				return Moderate;
			else if (loudness >= -20 && loudness <= -11)
				return Loud;
			else if (loudness >= -10 && loudness <= 0)
				return VeryLoud;
			else
				return null;
		}
	}

	public enum TransmissionLatency { // V2.
		Optimal, Increased, High, VeryHigh;

		public static TransmissionLatency o2(int latency) {// o2: A2 -> V(.)2 A2 = {0, 10, 20, ..., 5000} ms
			if (latency >= 0 && latency <= 1000)
				return Optimal;
			else if (latency >= 1001 && latency <= 2500)
				return Increased;
			else if (latency >= 2501 && latency <= 4000)
				return High;
			else if (latency >= 4001 && latency <= 5000)
				return VeryHigh;
			else
				return null;
		}
	}

	public enum NodeDistance {// V3.
		None, Short, Medium, Long, VeryLong;

		public static NodeDistance o3(int dst) {// o3: A3 -> V(.)3 A3 = {0, 1, 2, ..., 50} km
			if (dst == 0)
				return None;
			else if (dst >= 1 && dst <= 5)
				return Short;
			else if (dst >= 6 && dst <= 15)
				return Medium;
			else if (dst >= 16 && dst <= 35)
				return Long;
			else if (dst >= 36 && dst <= 50)
				return VeryLong;
			else
				return null;
		}
	}

	public enum MessageLength {// V4.
		Short, Regular, Long,
		Shr, Reg, Lng;

		public static MessageLength o4(MessageLength len) {// o4: A4 -> V(.)4 A4 = {Short, Regular, Long}
			if (len == Short)
				return Shr;
			else if (len == Regular)
				return Reg;
			else if (len == Long)
				return Lng;
			else
				return null;
		}
	}

	public enum SignalQuality {// V5.
		Unsatisfactory, Satisfactory, Medium, Good, Excellent,
		Uns, Sat, Med, Gd, Exc;
		
		public static SignalQuality o5(SignalQuality quality) {// o5: A5 -> V(.)5 A5 = {Unsatisfactory, Satisfactory, Medium, Good, Excellent}
			if (quality == Unsatisfactory)
				return Uns;
			else if (quality == Satisfactory)
				return Sat;
			else if (quality == Medium)
				return Med;
			else if (quality == Good)
				return Gd;
			else if (quality == Excellent)
				return Exc;
			else return null;
		}
	}
}
