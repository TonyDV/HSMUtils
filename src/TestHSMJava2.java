import java.io.*;
import java.net.Socket;

public class TestHSMJava2 {
    public static void main(String args[]) {
        final int messageLengthByte = 2;
        final int messageHeaderByte = 4;
        final int commandLengthByte = 2;
        final char delimiter = ';';

        System.out.println("<<< Main Method Entry >>>");
        String command1 = null;
        String command2 = null;
        String command3 = null;
        Socket socket = null;
        DataOutputStream out = null;
        DataInputStream  in = null;
        boolean messageSent = false;
        boolean messageReady = false;
        try {
            socket = new Socket("host", 1500);
            //socket = new Socket("localhost", 1500); //dummyListener
            System.out.println("<<< Socket >>> :" + socket);
            if (socket != null) {
                System.out.println("<<< Connected to HSM  >>>:"
                        + socket.isConnected());
                in = new DataInputStream (new BufferedInputStream(socket.getInputStream()));
                out = new DataOutputStream(new DataOutputStream(socket.getOutputStream()));

                String commandCode = "KS"; //command1.substring(4, 6);
                System.out.println("Command Code : " + commandCode);

                if (commandCode.equals("KI")) {
                    command1="0002KI109U7475636****B93B417194425DC51A6ACB";
                    command2="1568837395698701";
                    command3=";UA940CC330472671D0CDA49CCF19924DDX1";
                    out.writeUTF(command1 + command2 + command3);
                    System.out.println("Command KI was sent to HSM : ");

                } else if (commandCode.equals("KW")) {
                    command1 = "d563KW12U7475636CC30B93B417194425DC51A6AC";
                    String panPlusSequenceNo = "*";
                    String atc = "C4E4";
                    String transactionData = "00000000150000000000000008400000040000084023071401988073235C00C4E400000000000080";
                    String arqc = "E02075D4E325092F";
                    String arc = "0010";

                    int messageLength = command1.length() + panPlusSequenceNo.length()/2 + atc.length()/2
                            + 2 + transactionData.length()/2 + 1 + arqc.length()/2 + arc.length()/2;
                    String schemeID = command1.substring(7, 8);
                    System.out.println("Scheme ID = " + schemeID);
                    if (schemeID.equals("3")) {
                        messageLength += 3;
                    }
                    System.out.println("messageLength = " + messageLength);
                    out.write(0x00);
                    out.write(messageLength);
                    out.write(command1.getBytes("UTF-8"));

                    if (schemeID.equals("3")) {
                        out.write("0".getBytes("UTF-8"));
                        out.write(Integer.toHexString(panPlusSequenceNo.length()/2).getBytes("UTF-8"));
                    }
                    sendBytes(panPlusSequenceNo, out);
                    System.out.println();
                    if (schemeID.equals("3")) {
                        out.write(';');
                    }

                    sendBytes(atc, out);
                    System.out.println();

                    out.write(Integer.toHexString(transactionData.length()/2).getBytes("UTF-8"));
                    System.out.println(transactionData.length());
                    System.out.println("Transaction Data length " + Integer.toHexString(transactionData.length()/2));


                    sendBytes(transactionData, out);
                    System.out.println();
                    out.write(';');
                    sendBytes(arqc, out);
                    System.out.println();
                    sendBytes(arc, out);
                    System.out.println();


                    messageReady = !messageReady;

                } else if (commandCode.equals("KQ")) {
                    command1 = "d563KQ02U7475636****B93B417194425DC51A6AC";
                    String panPlusSequenceNo = "*";
                    String atc = "C4E4";
                    String un = "98807323";
                    String transactionData = "00000000150000000000000008400000040000084023071401988073235C00C4E400000000000080";
                    String arqc = "E02075D4E325092F";
                    String arc = "0010";

                    String modeFlag = command1.substring(6, 7);
                    System.out.println("Mode Flag = " + modeFlag);

                    String schemeID = command1.substring(7, 8);
                    System.out.println("Scheme ID = " + schemeID);



                    int messageLength = command1.length() + panPlusSequenceNo.length()/2 + atc.length()/2
                            + 2 + transactionData.length()/2 + 1 + arqc.length()/2;
                    if (!schemeID.equals("0")) {
                        messageLength += un.length()/2;
                    }

                    if (modeFlag.equals("1") || modeFlag.equals("2") || modeFlag.equals("4")) {
                        messageLength += arc.length()/2;
                    }


                    out.write(0x00);
                    out.write(messageLength);
                    out.write(command1.getBytes("UTF-8"));
                    sendBytes(panPlusSequenceNo, out);
                    sendBytes(atc, out);
                    if (!schemeID.equals("0")) {
                        sendBytes(un, out);
                    }

                    out.write(Integer.toHexString(transactionData.length()/2).getBytes("UTF-8"));
                    sendBytes(transactionData, out);
                    out.write(';');
                    sendBytes(arqc, out);
                    if (modeFlag.equals("1") || modeFlag.equals("2") || modeFlag.equals("4")) {
                        sendBytes(arc, out);
                    }

                    messageReady = !messageReady;

                } else if (commandCode.equals("KS")) {
                    command1 = "d563KS11U7BCA7B****FD97223BC9042785690E5C";
                    String panPlusSequenceNo = "*";
                    String dn = "0389";
                    String atc = "C4E4";
                    String un = "00000000";

                    int messageLength = command1.length() + panPlusSequenceNo.length()/2
                            + dn.length()/2 + atc.length()/2 + un.length()/2;
                    out.write(0x00);
                    out.write(messageLength);
                    out.write(command1.getBytes("UTF-8"));
                    sendBytes(panPlusSequenceNo, out);
                    sendBytes(dn, out);
                    sendBytes(atc, out);
                    sendBytes(un, out);

                    messageReady = !messageReady;

                }else if (commandCode.equals("A0")) {
                    command1 = "d563A00109U";
                    out.writeUTF(command1);
                    messageReady = !messageReady;
                }

                if (messageReady) {
                    out.flush();
                    messageSent =true;
                }

                if (messageSent) {

                     System.out.println("Reading response from HSM");

                    int length = readInt(messageLengthByte, in);
                    System.out.println("length = " + length);

                    String messageHeader = readChars(messageHeaderByte, in);
                    System.out.println("messageHeader = " + messageHeader);

                    String command = readChars(commandLengthByte, in);
                    System.out.println("command = " + command);

                    String responseCode = readChars(messageLengthByte, in);
                    System.out.println("responseCode = " + responseCode);

                    if (command.equals("KJ") && responseCode.equals("00")) {
                        length -= (messageHeaderByte + commandLengthByte + messageLengthByte);
                        System.out.println("1");
                        System.out.println("Reminds = " + readBytes( length, in).toUpperCase());
                    } else if (command.equals("KX") && responseCode.equals("01")) {
                        length -= (messageHeaderByte + commandLengthByte + messageLengthByte);
                        System.out.println("Reminds = " + readBytes( length, in).toUpperCase());
                    }

                    System.out.println("Response was read");

                }
                in.close();
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static int readInt(int lenght, DataInputStream in) {
        int response = 0;
        for (int i = 0; i < lenght; i++) {
                response += readInput(in);
        }
        return response;
    }
    public static String readChars(int lenght, DataInputStream in) {
        String response = "";
        for (int i = 0; i < lenght; i++) {
                response += (char)readInput(in);
        }
        return response;
    }
    public static String readBytes(int lenght, DataInputStream in) {
        String response = "";
        for (int i = 0; i < lenght; i++) {
            String sToByte = Integer.toHexString(readInput(in));

            response += ((sToByte.length() < 2 ) ? ("0" + sToByte ) : sToByte);
        }
        return response;
    }
    static int readInput(DataInputStream in) {
        try {
            return in.read();
            } catch (Exception e) {
                e.printStackTrace();
            }
        return -1;
        }

    static void sendToOut(char letter, DataOutputStream out) {
        try {
            out.write(letter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void sendBytes(String toOut, DataOutputStream out) {

        for (int y=0; y < toOut.length(); y += 2) {
            char letter = (char)Integer.parseInt(toOut.substring(y, y+2), 16);
            sendToOut(letter, out);
            //System.out.print(letter);
        }
    }

   }