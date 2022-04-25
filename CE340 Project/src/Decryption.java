// MUSTAFA ALAN 20180601003

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Decryption {
    static HashMap<String, String> charToBitsMap = new HashMap<>();
    static HashMap<String, String> charToBitsMapReverse = new HashMap<>();
    static HashMap<Integer,ArrayList<String>> dividedHashList = new HashMap<>();

    public static void main(String[] args) {
        initializeCharByteMapReverse();
        String allText = "";
        ArrayList<String> myTextArraylist;
        myTextArraylist = readLines(".\\src\\Ciphertext.txt");

        //READING EVERY LINE by 8 Char each 8 bits so 64 bits total
        divideIntoGroups(myTextArraylist,64);

        for (int i=0;i<dividedHashList.size();i++) {   //FOR EACH LINE in the txt file
            for (int j = 0; j < dividedHashList.get(i).size(); j++) {        //FOR EACH 8 CHARS
                try {

                    //get Byte List of 8 Chars
                    ArrayList<String> byteListPure = getEachCharAsBits(dividedHashList.get(i).get(j));
                    System.out.println(byteListPure);
                    int[] permutation = {6,4,2,8,7,5,3,1};

                    //Permutated Bytelist
                    ArrayList<String> byteList = PermutateString(byteListPure,permutation);

                    //Create key matrix
                    String key = "wq";
                    String[][] keyMatrix = createKeyMatrix(key);
                    System.out.println("KEYMATRIX");
                    for (int m = 0;m<keyMatrix.length;m++){
                        for (int n = 0;n<keyMatrix[0].length;n++){
                            System.out.print(keyMatrix[m][n]);
                        }
                        System.out.println();
                    }

                    //Generate K2 and K3
                    String k2 = generateK2(keyMatrix);
                    System.out.println("K2 :");
                    System.out.println(k2);
                    String k3 = generateK3(keyMatrix);
                    System.out.println("K3 :");
                    System.out.println(k3);

                    //SLR K2 to create K4 and SRR K3 to create K5
                    String k4 = LeftRotate(k2,3);
                    String k5 = RightRotate(k3,3);

                    //Split into 2 for the lists which are Xored with K4 and K5.
                    ArrayList<String> xoredLeftNibbleK4 = getPartOfTheList(byteList,0,3);
                    ArrayList<String> xoredRightNibbleK5 = getPartOfTheList(byteList,4,7);
                    System.out.println("XOREDLEFTK4 : "+ xoredLeftNibbleK4 );
                    System.out.println("XOREDRIGHTK5 : "+ xoredRightNibbleK5);

                    //XOR BACK xoredLeftNibbleK4 and xoredRightNibbleK5 to get back the not xored versions.
                    ArrayList<String> xoredRightNibbleK2 = XorEachByteInList(xoredRightNibbleK5,k5);
                    System.out.println("Xored Right Nibble with K2 : ");
                    System.out.println(xoredRightNibbleK2);

                    ArrayList<String> xoredLeftNibbleK3 = XorEachByteInList(xoredLeftNibbleK4,k4);
                    System.out.println("Xored Left Nibble with K3 : ");
                    System.out.println(xoredLeftNibbleK3);

                    //Now to get not Xored version of left and right nibble we xor them with K2 and K3.
                    ArrayList<String> leftNibble = XorEachByteInList(xoredLeftNibbleK3,k3);
                    System.out.println("Left Nibble : ");
                    System.out.println(leftNibble);

                    ArrayList<String> rightNibble = XorEachByteInList(xoredRightNibbleK2,k2);
                    System.out.println("Right Nibble: ");
                    System.out.println(rightNibble);

                    //Merge Left and Right nibble
                    ArrayList<String> mergedNibbles = mergeNibbles(rightNibble,leftNibble);
                    System.out.println("Merged Nibbles :");
                    System.out.println(mergedNibbles);

                    //Left rotate the mergedNibbles
                    ArrayList<String> leftRotatedMerged = LeftRotateAll(mergedNibbles);
                    System.out.println("LEFT ROTADED NIBBLES");
                    System.out.println(leftRotatedMerged);
                    System.out.println(leftRotatedMerged);
                    //DePermutate Again
                    ArrayList<String> dePermutatedOutput = DePermutateString(leftRotatedMerged,permutation);

                    //PermutateOutputToString
                    String output = bytesToString(dePermutatedOutput);

                    allText = allText+output;

                }catch (Exception ex){
                    allText = allText+"{{{DECRYPTION ERROR MISSING 8 CHARS HERE}}}";
                }


            }

            allText = allText+"\n";
        }
        System.out.println("\n||||||||||||||||||||||||| ALL TEXT |||||||||||||||||||||||||||");
        System.out.println(allText);
        createFile(allText);
    }

    public static void createFile(String text){
        try {
            FileWriter myWriter = new FileWriter(".\\src\\decryptedCipherText.txt");
            myWriter.write(text);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static String bytesToString(ArrayList<String> stringList){
        String output = "";
        for(String str : stringList){
            output = output + charToBitsMapReverse.get(str);
        }
        return output;

    }

    public static ArrayList<String> getEachCharAsBits(String byteGroups){
        ArrayList<String> byteList = new ArrayList<>();
        for(int i = 0;i<(byteGroups.length()/8);i++){
            String oneByte = "";
            for(int j = 0;j<(byteGroups.length()/8);j++){
                oneByte = oneByte + byteGroups.charAt((i*8)+j);
            }
            byteList.add(oneByte);
        }
        return byteList;
    }

    public static ArrayList<String> mergeNibbles(ArrayList<String> list1,ArrayList<String> list2){
        ArrayList<String> mergedList = new ArrayList<>();
        for(int i = 0;i<list1.size();i++){
            mergedList.add(list1.get(i));
            mergedList.add(list2.get(i));
        }
        return mergedList;
    }
    public static ArrayList<String> XorEachByteInList(ArrayList<String> byteList,String xorString){
        ArrayList<String> xoredByteList = new ArrayList<>();
        String[] xorStringList = xorString.split("");
        for (String str:byteList){
            String xoredString = "";
            String[] strList = str.split("");
            for(int i = 0;i<8;i++){
                xoredString = xoredString + (Integer.parseInt(strList[i])^Integer.parseInt(xorStringList[i]));
            }
            xoredByteList.add(xoredString);
        }
        return xoredByteList;
    }
    //Rotate all the 8 Characters array by 4 to right
    static ArrayList<String> LeftRotateAll(ArrayList<String> byteList){
        ArrayList<String> rotatedByteList = new ArrayList<>();
        for(String eachByte:byteList){
            rotatedByteList.add(LeftRotate(eachByte,4));
        }
        return rotatedByteList;
    }
    static String LeftRotate(String str, int d)
    {
        String ans = str.substring(d) + str.substring(0, d);
        return ans;
    }
    // function that rotates s towards right by d
    static String RightRotate(String str, int d)
    {
        return LeftRotate(str, str.length() - d);
    }

    public static String generateK2(String[][] keyMatrix){
        String x = "";
        String y = "";
        String k2 = "";
        for(int i = 0; i<4;i++){
            x = x+keyMatrix[i][3];
        }
        for(int i = 0; i<4;i++){
            x = x+keyMatrix[i][1];
        }
        for(int i = 0; i<4;i++){
            y = y+keyMatrix[i][0];
        }
        for(int i = 0; i<4;i++) {
            y = y + keyMatrix[i][2];
        }

        String[] xArray = x.split("");
        String[] yArray = y.split("");

        for(int i = 0;i<8;i++){
            k2 = k2 + (Integer.parseInt(xArray[i])^Integer.parseInt(yArray[i]));
        }
        return k2;
    }
    public static String generateK3(String[][] keyMatrix){
        String w = "";
        String z = "";
        String k3 = "";
        for(int i = 0; i<4;i++){
            w = w+keyMatrix[i][0];
        }
        for(int i = 0; i<4;i++){
            w = w+keyMatrix[i][1];
        }
        for(int i = 0; i<4;i++){
            z = z+keyMatrix[i][2];
        }
        for(int i = 0; i<4;i++) {
            z = z + keyMatrix[i][3];
        }

        String[] wArray = w.split("");
        String[] zArray = z.split("");

        for(int i = 0;i<8;i++){
            k3 = k3 + (Integer.parseInt(wArray[i])^Integer.parseInt(zArray[i]));
        }

        return k3;
    }

    public static String[][] createKeyMatrix(String key){
        String[] keyChars = key.split("");
        String keyValue = charToBitsMap.get(keyChars[0]) + charToBitsMap.get(keyChars[1]);
        String[][] keyMatrix = new String[4][4];
        String[] keyNumbers = keyValue.split("");
        int counter = 0;
        for(int i = 0; i<4;i++){
            for(int j = 0; j<4;j++){
                keyMatrix[i][j]=keyNumbers[counter];
                counter++;
            }
        }
        return keyMatrix;
    }

    public static ArrayList<String> getPartOfTheList(ArrayList<String> list, int startIndexIncluded,int endIndexIncluded){
        ArrayList<String> partOfTheList = new ArrayList<>();
        for(int i = startIndexIncluded;i<=endIndexIncluded;i++){
            partOfTheList.add(list.get(i));
        }
        return partOfTheList;
    }
    public static ArrayList<String> StringToByteArray(String string) {
        //!!!!!!!!!!!!!!! different than Test class
        ArrayList<String> charList = new ArrayList<>();

        char[] chars = string.toCharArray();
        for (char c : chars) {
            String binary = Integer.toBinaryString(c);
            String formatted = String.format("%8s", binary);
            String output = formatted.replaceAll(" ", "0");
            charList.add(output);
            System.out.println(output);
        }
        return charList;
    }
    public static ArrayList<String> PermutateString(ArrayList<String> byteList, int[] permutation)
    {
        ArrayList<String> permutatedByteList = new ArrayList<>();

            for (int i = 1; i < byteList.size() + 1; i++) {
                for (int j = 0; j < byteList.size() ; j++) {
                    int positionNo = permutation[j];
                    if (positionNo == i) {
                        permutatedByteList.add(byteList.get(j));
                    }
                }
            }

        return permutatedByteList;
    }

    public static ArrayList<String> DePermutateString(ArrayList<String> mergedXoredList,int[] permutation)
    {
        ArrayList<String> depermutatedList = new ArrayList<>();
        String[] outputArray = new String[8];
        for(int i = 0;i<mergedXoredList.size();i++){
            outputArray[i] = mergedXoredList.get(i);
        }
        for(int i = 1 ; i< mergedXoredList.size()+1;i++){
            for(int j = 0 ; j< mergedXoredList.size();j++){
                if(i==permutation[j]){
                    outputArray[j] = mergedXoredList.get(i-1);
                }
            }
        }
        depermutatedList.addAll(Arrays.asList(outputArray));

        return depermutatedList;
    }

    public static void divideIntoGroups(ArrayList<String> myTextArrayList,int divideNumber){
        for (int i = 0 ; i<myTextArrayList.size();i++){
            divideString(myTextArrayList.get(i),i,divideNumber);
        }
    }

    //We use this for each line to divide into 8 chars and if any missing char put '*' instead
    static void divideString(String str, int lineNumber,int n)
    {
        System.out.println("Start of line "+(lineNumber+1)+" '"+ str+"' ");
        int str_size = str.length();

        ArrayList<String> lineStringArrayList = new ArrayList<>();

        // Check if string can be divided in N equal parts
        if (str_size % n != 0) {
            System.out.println("Invalid Input: String size"
                    + "is not divisible by n");
            int remainingNumber = (n-(str_size%n));
            for (int i = 0; i < remainingNumber;i++){
                str = str+"*";
            }
            str_size = str.length();
        }

        //We reset each group  and add in into the line list
        String group = "";
        for (int i = 0; i < str_size+1; i++) {
            if (i % n == 0 && (i!=0)){
                System.out.println(group);
                lineStringArrayList.add(group);
                group = "";
            }
            if(i == str_size){
                break;
            }
            group = group+str.charAt(i);

        }
        System.out.println("End of line "+ (lineNumber+1));
        dividedHashList.put(lineNumber,lineStringArrayList);
    }
    public static void initializeCharByteMapReverse(){
        String[] myCharList = {"A","B","C","Ç","D","E","F","G","Ğ","H","I","İ","J","K","L","M","N","O","Ö","P","R","S","Ş","T","U","Ü","V","Y","Z"
                ,".",",","(",")","!",";",":","'","\"","-","?","$","@","%","a","b","c","ç","d","e","f","g","ğ","h","ı","i","j","k","l","m","n","o","ö","p"
                ,"r","s","ş","t","u","ü","v","y","z","Q","W","q","w","X","x"," ","*","…","“","”"};
        for(int j = 0; j<myCharList.length;j++){
            int dec=(j+1);
            String result= "00000000";
            int i=result.length()-1;
            while(dec!=0)
            {
                char a[]=result.toCharArray();
                a[i--]= String.valueOf(dec%2).charAt(0);
                result=new String(a);
                dec=dec/2;
            }
            System.out.println(j + " , " + myCharList[j]+ " , "+result);
            charToBitsMapReverse.put(result,myCharList[j]);
            charToBitsMap.put(myCharList[j],result);
        }
    }
    public static ArrayList<String> readLines(String filename) {
        ArrayList<String> result = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename, StandardCharsets.UTF_8))) {
            while (br.ready()) {
                result.add(br.readLine());
            }
        }
        catch (Exception ex){
            System.out.println("Error about reading text");
        }
        return result;
    }
}
