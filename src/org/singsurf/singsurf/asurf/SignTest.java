package org.singsurf.singsurf.asurf;

import java.util.List;

public final class SignTest {

	/**
	 * Mimics C strcmp
	 * @param actualStr
	 * @param testStr
	 * @return false is char arrays the same
	 */
	private static boolean strcmp(char[] actualStr, char[] testStr) {
		if(actualStr.length != testStr.length) return true;
		for(int i=0;i<actualStr.length;++i)
			if(actualStr[i]!=testStr[i]) return true;
		return false;
	}

	/**
	 * Mimics C strcpy
	 * @param reorderStr
	 * @param testStr
	 */
	private static void strcpy(char[] reorderStr, char[] testStr) {
		System.arraycopy(testStr, 0, reorderStr, 0, testStr.length);
//		for(int i=0;i<testStr.length;++i)
//			reorderStr[i]=testStr[i];
	}

	private static int strlen(char[] testStr) {
		return testStr.length;
	}

	static boolean TestSignsPerform(char actualStr[], char testStr[])
	{
		return(!strcmp(actualStr,testStr));
	}

//	static boolean TestSignsReorder1(char actualStr[],char testStr[],int len,int width,int order[])
//	{
//		char reorderStr[] = new char[testStr.length];
//		int i,j;
//	
//		strcpy(reorderStr,testStr);
//		for(i=0;i<len;++i)
//			for(j=0;j<width;++j)
//				reorderStr[i*(width+1)+j] = actualStr[order[i]*(width+1)+j];
//		return(!strcmp(reorderStr,testStr));
//	}
//
//	static boolean TestSignsReorder2(char actualStr[],char testStr[],int len,int width,int order[])
//	{
//		char reorderStr[] = new char[testStr.length];
//		int i,j;
//	
//		strcpy(reorderStr,testStr);
//		for(i=0;i<len;++i)
//			for(j=0;j<width;++j)
//				reorderStr[i*(width+1)+j] = actualStr[order[i]*(width+1)+j];
//		return(!strcmp(reorderStr,testStr));
//	}
//
//	static boolean TestSignsReorder3(char actualStr[],char testStr[],int len,int width,int order[])
//	{
//		char reorderStr[] = new char[testStr.length];
//		int i,j;
//	
//		strcpy(reorderStr,testStr);
//		for(i=0;i<len;++i)
//			for(j=0;j<width;++j)
//				reorderStr[i*(width+1)+j] = actualStr[order[i]*(width+1)+j];
//		return(!strcmp(reorderStr,testStr));
//	}
//
//	static boolean TestSignsReorder4(char actualStr[],char testStr[],int len,int width,int order[])
//	{
//		char reorderStr[] = new char[testStr.length];
//		int i,j;
//	
//		strcpy(reorderStr,testStr);
//		for(i=0;i<len;++i)
//			for(j=0;j<width;++j)
//				reorderStr[i*(width+1)+j] = actualStr[order[i]*(width+1)+j];
//		return(!strcmp(reorderStr,testStr));
//	}
//
//	static boolean TestSignsReorder5(char actualStr[],char testStr[],int len,int width,int order[])
//	{
//		char reorderStr[] = new char[testStr.length];
//		int i,j;
//	
//		strcpy(reorderStr,testStr);
//		for(i=0;i<len;++i)
//			for(j=0;j<width;++j)
//				reorderStr[i*(width+1)+j] = actualStr[order[i]*(width+1)+j];
//		return(!strcmp(reorderStr,testStr));
//	}
//
//	static boolean TestSignsReorder6(char actualStr[],char testStr[],int len,int width,int order[])
//	{
//		char reorderStr[] = new char[testStr.length];
//		int i,j;
//	
//		strcpy(reorderStr,testStr);
//		for(i=0;i<len;++i)
//			for(j=0;j<width;++j)
//				reorderStr[i*(width+1)+j] = actualStr[order[i]*(width+1)+j];
//		return(!strcmp(reorderStr,testStr));
//	}

	static boolean TestSignsReorder(char actualStr[],char testStr[],int len,int width,int order[])
	{
		char reorderStr[] = new char[testStr.length];
		int i,j;
	
		strcpy(reorderStr,testStr);
		for(i=0;i<len;++i)
			for(j=0;j<width;++j)
				reorderStr[i*(width+1)+j] = actualStr[order[i]*(width+1)+j];
		return(!strcmp(reorderStr,testStr));
	}
//
//	static boolean TestSignsReorder(int num,char actualStr[],char testStr[],int len,int width,int order[])
//	{
//		switch(num)
//		{
//		case 1: return TestSignsReorder1(actualStr,testStr,len,width,order);
//		case 2: return TestSignsReorder2(actualStr,testStr,len,width,order);
//		case 3: return TestSignsReorder3(actualStr,testStr,len,width,order);
//		case 4: return TestSignsReorder4(actualStr,testStr,len,width,order);
//		case 5: return TestSignsReorder5(actualStr,testStr,len,width,order);
//		case 6: return TestSignsReorder6(actualStr,testStr,len,width,order);
//		case 7: return TestSignsReorder7(actualStr,testStr,len,width,order);
//		default: return TestSignsReorder7(actualStr,testStr,len,width,order);
//		}
//	}

	static boolean TestSignsCycle(char actualStr[],int count,char testStr[],int len,int width,int order[])
	{
		int i1,i2,i3,i4,i5,i6,i7,i8;
	
		switch(len)
		{
		case 1:
			for(i1=0;i1<count;++i1)
			{
				order[0] = i1;
				if(TestSignsReorder(actualStr,testStr,len,width,order))
					return true;
			}
			return false;
		case 2:
			for(i1=0;i1<count;++i1)
				for(i2=0;i2<count;++i2)
				{
					if(i1 == i2) continue;
					order[0] = i1;
					order[1] = i2;
					if(TestSignsReorder(actualStr,testStr,len,width,order))
						return true;
				}
			return false;
		case 3:
			for(i1=0;i1<count;++i1)
				for(i2=0;i2<count;++i2)
				{ if(i1 == i2) continue;
				for(i3=0;i3<count;++i3)
				{
					if(i3 == i1 || i3 == i2) continue;
					order[0] = i1;
					order[1] = i2;
					order[2] = i3;
					if(TestSignsReorder(actualStr,testStr,len,width,order))
						return true;
				}
				}
			return false;
		case 4:
			for(i1=0;i1<count;++i1)
				for(i2=0;i2<count;++i2)
				{ if(i1 == i2) continue;
				for(i3=0;i3<count;++i3)
				{ if(i3 == i1 || i3 == i2) continue;
				for(i4=0;i4<count;++i4)
				{ if(i4 == i1 || i4 == i2 || i4 == i3) continue;
				order[0] = i1;
				order[1] = i2;
				order[2] = i3;
				order[3] = i4;
				if(TestSignsReorder(actualStr,testStr,len,width,order))
					return true;
				}
				}
				}
			return false;
		case 5:
			for(i1=0;i1<count;++i1)
				for(i2=0;i2<count;++i2)
				{ if(i1 == i2) continue;
				for(i3=0;i3<count;++i3)
				{ if(i3 == i1 || i3 == i2) continue;
				for(i4=0;i4<count;++i4)
				{ if(i4 == i1 || i4 == i2 || i4 == i3) continue;
				for(i5=0;i5<count;++i5)
				{ if(i5 == i1 || i5 == i2 || i5 == i3 || i5 == i4) continue;
				order[0] = i1;
				order[1] = i2;
				order[2] = i3;
				order[3] = i4;
				order[4] = i5;
				if(TestSignsReorder(actualStr,testStr,len,width,order))
					return true;
				}
				}
				}
				}
			return false;
		case 6:
			for(i1=0;i1<count;++i1)
				for(i2=0;i2<count;++i2)
				{ if(i1 == i2) continue;
				for(i3=0;i3<count;++i3)
				{ if(i3 == i1 || i3 == i2) continue;
				for(i4=0;i4<count;++i4)
				{ if(i4 == i1 || i4 == i2 || i4 == i3) continue;
				for(i5=0;i5<count;++i5)
				{ if(i5 == i1 || i5 == i2 || i5 == i3 || i5 == i4) continue;
				for(i6=0;i6<count;++i6)
				{ if(i6 == i1 || i6 == i2 || i6 == i3 || i6 == i4 || i6 == i5) continue;
				order[0] = i1;
				order[1] = i2;
				order[2] = i3;
				order[3] = i4;
				order[4] = i5;
				order[5] = i6;
				if(TestSignsReorder(actualStr,testStr,len,width,order))
					return true;
				}
				}
				}
				}
				}
			return false;
		case 7:
			for(i1=0;i1<count;++i1)
				for(i2=0;i2<count;++i2)
				{ if(i1 == i2) continue;
				for(i3=0;i3<count;++i3)
				{ if(i3 == i1 || i3 == i2) continue;
				for(i4=0;i4<count;++i4)
				{ if(i4 == i1 || i4 == i2 || i4 == i3) continue;
				for(i5=0;i5<count;++i5)
				{ if(i5 == i1 || i5 == i2 || i5 == i3 || i5 == i4) continue;
				for(i6=0;i6<count;++i6)
				{ if(i6 == i1 || i6 == i2 || i6 == i3 || i6 == i4 || i6 == i5) continue;
				for(i7=0;i7<count;++i7)
				{ if(i7 == i1 || i7 == i2 || i7 == i3 || i7 == i4 
						|| i7 == i5 || i7 == i6) continue;
				order[0] = i1;
				order[1] = i2;
				order[2] = i3;
				order[3] = i4;
				order[4] = i5;
				order[5] = i6;
				order[6] = i7;
				if(TestSignsReorder(actualStr,testStr,len,width,order))
					return true;
				}
				}
				}
				}
				}
				}
			return false;
		case 8:
			for(i1=0;i1<count;++i1)
				for(i2=0;i2<count;++i2)
				{ if(i1 == i2) continue;
				for(i3=0;i3<count;++i3)
				{ if(i3 == i1 || i3 == i2) continue;
				for(i4=0;i4<count;++i4)
				{ if(i4 == i1 || i4 == i2 || i4 == i3) continue;
				for(i5=0;i5<count;++i5)
				{ if(i5 == i1 || i5 == i2 || i5 == i3 || i5 == i4) continue;
				for(i6=0;i6<count;++i6)
				{ if(i6 == i1 || i6 == i2 || i6 == i3 || i6 == i4 || i6 == i5) continue;
				for(i7=0;i7<count;++i7)
				{ if(i7 == i1 || i7 == i2 || i7 == i3 || i7 == i4 
						|| i7 == i5 || i7 == i6) continue;
				for(i8=0;i8<count;++i8)
				{ if(i8 == i1 || i8 == i2 || i8 == i3 || i8 == i4 
						|| i8 == i5 || i8 == i6 || i8 == i7) continue;
				order[0] = i1;
				order[1] = i2;
				order[2] = i3;
				order[3] = i4;
				order[4] = i5;
				order[5] = i6;
				order[6] = i7;
				order[7] = i8;
				if(TestSignsReorder(actualStr,testStr,len,width,order))
					return true;
				}
				}
				}
				}
				}
				}
				}
			return false;
		default:
			System.err.printf("TestNodesCycle: Bad length %d\n",len);
		}
		return false;
	}

	static boolean TestSigns(char actualStr[],int count,int width,char testStr[],char signStr[],char rotStr[],int order[])
	{
		int i,j,k;
		int len,signLen,rotLen;
	
		len = (strlen(testStr)+1)/(width+1);
		signLen = (strlen(signStr)+1)/(width+1);
		rotLen  = (strlen(rotStr)+1)/(width+1);
		if(len>8)
		{
			System.err.printf("TestNodes: len to big %d (%s)\n",len,testStr);
			return false;
		}
		if(count < len) return false;
	
		char rotTest[] = new char[testStr.length];
		char signTest[] = new char[testStr.length];
		//strcpy(rotTest,testStr); /* just to get the right size */
	
		for(i=0;i<signLen;++i)
		{
			strcpy(signTest,testStr);
			for(j=0;j<width;++j)
				if(signStr[i*(width+1)+j] == '-')
					for(k=0;k<len;++k)
					{
						if(     testStr[k*(width+1)+j] == '+')
							signTest[k*(width+1)+j] = '-';
						else if(testStr[k*(width+1)+j] == '-')
							signTest[k*(width+1)+j] = '+';
					}
	
			/* fixed signs, now fix rotation */
	
			for(j=0;j<rotLen;++j)
			{
				int offset;
				for(k=0;k<width;++k)
				{
					int l;
					offset = rotStr[j*(width+1)+k] - 'a';
					for(l=0;l<len;++l)
						rotTest[l*(width+1)+k] = signTest[l*(width+1)+offset];
				}
	
				if(TestSignsCycle(actualStr,count,rotTest,len,width,order))
					return true;
			}
		}
		return false;
	}

	public static boolean TestSigns(char[] signStr, int count, int width,
			String string, String string2, String string3, int[] order) {
	
		return TestSigns(signStr, count, width,
				string.toCharArray(), string2.toCharArray(), string3.toCharArray(), order);
	}

	static void BuildNodeSigns(Node_info bn[],int count,char testStr[])
	{
		int i;
	
		if(count>20)
		{
			System.err.printf("BuildNodeSigns: Error count too high %d max 20\n",count);
			throw new IllegalArgumentException("BuildNodeSigns count="+count);
		}
		for(i=0;i<count;++i)
		{
			if(     bn[i].sol.getDx() >  0) testStr[i*4+0] = '+';
			else if(bn[i].sol.getDx() == 0) testStr[i*4+0] = '0';
			else if(bn[i].sol.getDx() <  0) testStr[i*4+0] = '-';
	
			if(     bn[i].sol.getDy() >  0) testStr[i*4+1] = '+';
			else if(bn[i].sol.getDy() == 0) testStr[i*4+1] = '0';
			else if(bn[i].sol.getDy() <  0) testStr[i*4+1] = '-';
	
			if(     bn[i].sol.getDz() >  0) testStr[i*4+2] = '+';
			else if(bn[i].sol.getDz() == 0) testStr[i*4+2] = '0';
			else if(bn[i].sol.getDz() <  0) testStr[i*4+2] = '-';
			testStr[i*4+3] = '|';
		}
		testStr[count*4] = '\0';
	}


	public static char[] BuildNodeSigns2(Node_info bn[],int count)
	{
		int i;
		char testStr[] = new char[count*4+1];
	
		for(i=0;i<count;++i)
		{
			if(     bn[i].sol.getDx() >  0) testStr[i*4+0] = '+';
			else if(bn[i].sol.getDx() == 0) testStr[i*4+0] = '0';
			else if(bn[i].sol.getDx() <  0) testStr[i*4+0] = '-';
	
			if(     bn[i].sol.getDy() >  0) testStr[i*4+1] = '+';
			else if(bn[i].sol.getDy() == 0) testStr[i*4+1] = '0';
			else if(bn[i].sol.getDy() <  0) testStr[i*4+1] = '-';
	
			if(     bn[i].sol.getDz() >  0) testStr[i*4+2] = '+';
			else if(bn[i].sol.getDz() == 0) testStr[i*4+2] = '0';
			else if(bn[i].sol.getDz() <  0) testStr[i*4+2] = '-';
			testStr[i*4+3] = '|';
		}
		//testStr[count*4-1] = '\0';
		return testStr;
	}

	public static void BuildSolSigns(Sol_info bn[],int count,char testStr[])
	{
		int i;
	
		if(count>20)
		{
			System.err.printf("BuildNodeSigns: Error count too high %d max 20\n",count);
			throw new IllegalArgumentException("BuildNodeSigns: count too high "+count);
		}
	
		for(i=0;i<count;++i)
		{
			if(     bn[i].getDx() >  0) testStr[i*4+0] = '+';
			else if(bn[i].getDx() == 0) testStr[i*4+0] = '0';
			else if(bn[i].getDx() <  0) testStr[i*4+0] = '-';
	
			if(     bn[i].getDy() >  0) testStr[i*4+1] = '+';
			else if(bn[i].getDy() == 0) testStr[i*4+1] = '0';
			else if(bn[i].getDy() <  0) testStr[i*4+1] = '-';
	
			if(     bn[i].getDz() >  0) testStr[i*4+2] = '+';
			else if(bn[i].getDz() == 0) testStr[i*4+2] = '0';
			else if(bn[i].getDz() <  0) testStr[i*4+2] = '-';
			testStr[i*4+3] = '|';
		}
		testStr[count*4] = '\0';
	}

	public static char[] BuildNodeSigns2(List<Node_info> nodes) {
		int count = nodes.size();
		int i=0;
		char testStr[] = new char[count*4+1];
		
		for(Node_info node:nodes)
		{		
			if(     node.sol.getDx() >  0) testStr[i*4+0] = '+';
			else if(node.sol.getDx() == 0) testStr[i*4+0] = '0';
			else if(node.sol.getDx() <  0) testStr[i*4+0] = '-';
	
			if(     node.sol.getDy() >  0) testStr[i*4+1] = '+';
			else if(node.sol.getDy() == 0) testStr[i*4+1] = '0';
			else if(node.sol.getDy() <  0) testStr[i*4+1] = '-';
	
			if(     node.sol.getDz() >  0) testStr[i*4+2] = '+';
			else if(node.sol.getDz() == 0) testStr[i*4+2] = '0';
			else if(node.sol.getDz() <  0) testStr[i*4+2] = '-';
			testStr[i*4+3] = '|';
			++i;
		}
		//testStr[count*4-1] = '\0';
		return testStr;
	}

	public static void BuildSolSigns(List<Sol_info> bn,char testStr[])
	{
		int i;
		int count = bn.size();
		if(count>20)
		{
			System.err.printf("BuildNodeSigns: Error count too high %d max 20\n",count);
			throw new IllegalArgumentException("BuildNodeSigns: count too high "+count);
		}
	
		for(i=0;i<count;++i)
		{
			if(     bn.get(i).getDx() >  0) testStr[i*4+0] = '+';
			else if(bn.get(i).getDx() == 0) testStr[i*4+0] = '0';
			else if(bn.get(i).getDx() <  0) testStr[i*4+0] = '-';
	
			if(     bn.get(i).getDy() >  0) testStr[i*4+1] = '+';
			else if(bn.get(i).getDy() == 0) testStr[i*4+1] = '0';
			else if(bn.get(i).getDy() <  0) testStr[i*4+1] = '-';
	
			if(     bn.get(i).getDz() >  0) testStr[i*4+2] = '+';
			else if(bn.get(i).getDz() == 0) testStr[i*4+2] = '0';
			else if(bn.get(i).getDz() <  0) testStr[i*4+2] = '-';
			testStr[i*4+3] = '|';
		}
		testStr[count*4] = '\0';
	}


//	static boolean TestSigns1(char[] signStr, int count, int width,
//			String string, String string2, String string3, int[] order) {
//	
//		return TestSigns1(signStr, count, width,
//				string.toCharArray(), string2.toCharArray(), string3.toCharArray(), order);
//	}
//
//	static boolean TestSigns3(char[] signStr, int count, int width,
//			String string, String string2, String string3, int[] order) {
//	
//		return TestSigns3(signStr, count, width,
//				string.toCharArray(), string2.toCharArray(), string3.toCharArray(), order);
//	}
//
//	static boolean TestSigns1(char actualStr[],int count,int width,char testStr[],char signStr[],char rotStr[],int order[])
//	{
//		//Boxclev.TestSignNum = 1;
//		return TestSigns(1,actualStr,count,width,testStr,signStr,rotStr,order);
//	}
//
//	static boolean TestSigns2(char actualStr[],int count,int width,char testStr[],char signStr[],char rotStr[],int order[])
//	{
//		//Boxclev.TestSignNum = 2;
//		return TestSigns(2,actualStr,count,width,testStr,signStr,rotStr,order);
//	}
//
//	static boolean TestSigns3(char actualStr[],int count,int width,char testStr[],char signStr[],char rotStr[],int order[])
//	{
//		//Boxclev.TestSignNum = 3;
//		return TestSigns(3,actualStr,count,width,testStr,signStr,rotStr,order);
//	}
//
//	static boolean TestSigns4(char[] signStr, int count, int width,
//			String string, String string2, String string3, int[] order) {
//	
//		return TestSigns4(signStr, count, width,
//				string.toCharArray(), string2.toCharArray(), string3.toCharArray(), order);
//	}
//
//	static boolean TestSigns6(char[] signStr, int count, int width,
//			String string, String string2, String string3, int[] order) {
//		return TestSigns6(signStr, count, width,
//				string.toCharArray(), string2.toCharArray(), string3.toCharArray(), order);
//	}
//
//	static boolean TestSigns5(char[] signStr, int count, int width,
//			String string, String string2, String string3, int[] order) {
//		return TestSigns5(signStr, count, width,
//				string.toCharArray(), string2.toCharArray(), string3.toCharArray(), order);
//	}
//
//	static boolean TestSigns4(char actualStr[],int count,int width,char testStr[],char signStr[],char rotStr[],int order[])
//	{
//		//Boxclev.TestSignNum = 4;
//		return TestSigns(4,actualStr,count,width,testStr,signStr,rotStr,order);
//	}
//
//	static boolean TestSigns5(char actualStr[],int count,int width,char testStr[],char signStr[],char rotStr[],int order[])
//	{
//		//Boxclev.TestSignNum = 5;
//		return TestSigns(5,actualStr,count,width,testStr,signStr,rotStr,order);
//	}
//
//	static boolean TestSigns6(char actualStr[],int count,int width,char testStr[],char signStr[],char rotStr[],int order[])
//	{
//		//Boxclev.TestSignNum = 6;
//		return TestSigns(6,actualStr,count,width,testStr,signStr,rotStr,order);
//	}
//
//	static boolean TestSigns7(char actualStr[],int count,int width,char testStr[],char signStr[],char rotStr[],int order[])
//	{
//		//Boxclev.TestSignNum = 7;
//		return TestSigns(7,actualStr,count,width,testStr,signStr,rotStr,order);
//	}

}
