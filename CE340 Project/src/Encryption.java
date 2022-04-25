// MUSTAFA ALAN 20180601003
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class Encryption {
    static HashMap<String, String> charToBitsMap = new HashMap<>();
    static HashMap<Integer,ArrayList<String>> dividedHashList = new HashMap<>();

    public static void main(String[] args) {
        initializeCharByteMap();
        String allText = "";
        ArrayList<String> myTextArraylist;
        myTextArraylist = readLines(".\\src\\Plaintext.txt");


        //READING EVERY LINE by 8 Char
        divideIntoGroups(myTextArraylist);

        for (int i=0;i<dividedHashList.size();i++){     //FOR EACH LINE in the txt file
            for (int j=0;j<dividedHashList.get(i).size();j++){    //FOR EACH 8 CHARS in a LİNE
                try{

                    //PERMUTATION PART
                    int[] permutation = {6,4,2,8,7,5,3,1};
                    System.out.println("BEFORE Permutated :");
                    System.out.println(dividedHashList.get(i).get(j));
                    String shuffledString = PermutateString(dividedHashList.get(i).get(j),permutation);
                    System.out.println("Permutated: ");
                    System.out.println(shuffledString);


                    //GET BITS REPRESENTATION OF THE PERMUTATED STRING
                    ArrayList<String> byteList = StringToByteArray(shuffledString);
                    System.out.println("Byte List : ");
                    System.out.println(byteList);

                    //right shift rotate for all bytes
                    ArrayList<String> rotatedByteList = RightRotateAll(byteList);
                    System.out.println("Rotated Byte List : ");
                    System.out.println(rotatedByteList);

                    //Create key matrix
                    String key = "wq";
                    String[][] keyMatrix = createKeyMatrix(key);

                    //Generate K2 and K3
                    String k2 = generateK2(keyMatrix);
                    System.out.println("K2 :");
                    System.out.println(k2);
                    String k3 = generateK3(keyMatrix);
                    System.out.println("K3 :");
                    System.out.println(k3);

                    //SLR K2 to create K4 and SRR K3 to create K5
                    String k4 = LeftRotate(k2,3);
                    System.out.println("K4 :");
                    System.out.println(k4);
                    String k5 = RightRotate(k3,3);
                    System.out.println("K5 :");
                    System.out.println(k5);



                    //All Bytes before creating Nibbles
                    System.out.println("Before Nibbles :");
                    System.out.println(rotatedByteList);
                    //Create Left and Right Nibbles
                    ArrayList<String> leftNibble = createLeftNibble(rotatedByteList);
                    ArrayList<String> rightNibble =createRightNibble(rotatedByteList);
                    System.out.println("Right nibble :");
                    System.out.println(rightNibble);
                    System.out.println("Left nibble : ");
                    System.out.println(leftNibble);

                    //Xor Right Nibble with K2
                    ArrayList<String> xoredRightNibbleK2 = XorEachByteInList(rightNibble,k2);
                    System.out.println("Xored Right Nibble with K2: ");
                    System.out.println(xoredRightNibbleK2);

                    //Xor Left Nibble with K3
                    ArrayList<String> xoredLeftNibbleK3 = XorEachByteInList(leftNibble,k3);
                    System.out.println("Xored Left Nibble with K3 : ");
                    System.out.println(xoredLeftNibbleK3);


                    //Xor Left Nibble with K4
                    ArrayList<String> xoredLeftNibbleK4 = XorEachByteInList(xoredLeftNibbleK3,k4);
                    System.out.println("Xored Left Nibble with K4 second time : ");
                    System.out.println(xoredLeftNibbleK4);

                    //Xor Right Nibble with K5
                    ArrayList<String> xoredRightNibbleK5 = XorEachByteInList(xoredRightNibbleK2,k5);
                    System.out.println("Xored Right Nibble with K5 second time : ");
                    System.out.println(xoredRightNibbleK5);

                    //Merge the xored lists
                    System.out.println("----------------------");
                    ArrayList<String> mergedXoredList = mergeXoredLists(xoredLeftNibbleK4,xoredRightNibbleK5);
                    System.out.println(mergedXoredList);
                    System.out.println("----------------------");

                    String dePermutatedText = DePermutateString(mergedXoredList,permutation);

                    //Create Cyphered Text Added to All Text
                    allText = allText+dePermutatedText;

                }catch (Exception ex){
                    allText = allText+"{{{ENCRYPTION ERROR MISSING 8 CHARS HERE}}}";
                }
            }

            allText = allText+"\n";
        }
        System.out.println("\n||||||||||||||||||||||||| ALL TEXT ENCRYPTED |||||||||||||||||||||||||||");
        System.out.println(allText);
        createFile(allText);


    }
    public static void createFile(String text){
        try {
            FileWriter myWriter = new FileWriter(".\\src\\Ciphertext.txt");
            myWriter.write(text);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static ArrayList<String> mergeXoredLists(ArrayList<String> list1,ArrayList<String> list2){
        list1.addAll(list2);
        return list1;
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

    public static ArrayList<String> createLeftNibble(ArrayList<String> byteList){
        ArrayList<String> leftNibble = new ArrayList<>();

        for(int i = 1; i<byteList.size();i+=2){
            leftNibble.add(byteList.get(i));
        }
        return leftNibble;
    }

    public static ArrayList<String> createRightNibble(ArrayList<String> byteList){
        ArrayList<String> rightNibble = new ArrayList<>();
        for(int i = 0 ; i<byteList.size();i+=2){
            rightNibble.add(byteList.get(i));
        }
        return rightNibble;
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

    //Rotate all the 8 Characters array by 4 to right
    static ArrayList<String> RightRotateAll(ArrayList<String> byteList){
        ArrayList<String> rotatedByteList = new ArrayList<>();
        for(String eachByte:byteList){
            rotatedByteList.add(RightRotate(eachByte,4));
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

    public static ArrayList<String> StringToByteArray(String string) {
        ArrayList<String> charList = new ArrayList<>();
        String[] letters = string.split("");
        for (String str:letters) {
            if(charToBitsMap.get(str) == null){
                charList.add("00100000");
            }else{
                charList.add(charToBitsMap.get(str));
            }

        }
        return charList;
    }

    //Permutate the strings
    public static String PermutateString(String string,int[] permutation)
    {
        String myNewString = "";
        for(int i = 1 ; i< string.length()+1;i++){
            for(int j = 0 ; j< string.length();j++){
                int positionNo = permutation[j];
                if(positionNo == i){
                    myNewString = myNewString+string.charAt(j);
                }
            }
        }
        return myNewString;
    }

    public static String DePermutateString(ArrayList<String> mergedXoredList,int[] permutation)
    {
        String[] outputArray = new String[8];
        for(int i = 0;i<mergedXoredList.size();i++){
            outputArray[i] = mergedXoredList.get(i);
        }

        String myNewString = "";
        for(int i = 1 ; i< mergedXoredList.size()+1;i++){
            for(int j = 0 ; j< mergedXoredList.size();j++){
                if(i==permutation[j]){
                    outputArray[j] = mergedXoredList.get(i-1);
                }
            }
        }
        for (String str:outputArray){
            myNewString = myNewString+str;

        }

        return myNewString;
    }




    public static ArrayList<String> readLines(String filename) {
        ArrayList<String> result = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename,StandardCharsets.UTF_8))) {
            while (br.ready()) {
                result.add(br.readLine());
            }
        }
        catch (Exception ex){
            System.out.println("Error about reading text");
        }
        return result;
    }

    //All the lines are here as a string list, and we use other method for each of them
    public static void divideIntoGroups(ArrayList<String> myTextArrayList){
        for (int i = 0 ; i<myTextArrayList.size();i++){
            divideString(myTextArrayList.get(i),i,8);
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
                str = str+" ";
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

    public static void initializeCharByteMap(){
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
            charToBitsMap.put(myCharList[j],result);
        }
    }
}
