package com.bn.util;

import com.bn.parameter.Constant;
import com.bn.parameter.Preference;
import com.bn.tool_package.MyConverter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by 13273 on 2017/7/24.
 *
 */

public class NetConnectionUtil {
    private static Socket ss = null;
    private static DataInputStream din = null;
    private static DataOutputStream dout = null;
    private static void establishConnection() throws Exception {
        ss = new Socket();
        SocketAddress address = new InetSocketAddress(Preference.serverIP, 10055);
        //超过1秒无法连接便放弃 不用该方法会导致连接时间过长 线程阻塞
        ss.connect(address, 1000);
        din = new DataInputStream(ss.getInputStream());
        dout = new DataOutputStream(ss.getOutputStream());
    }
    private static void closeConnection() {
        if (dout != null) {
            try {
                dout.flush();
                dout.close();
            } catch (Exception e) { e.printStackTrace(); }
        }

        if (din != null) {
            try {din.close();} catch (Exception e) { e.printStackTrace();}
        }
        if (ss != null) {
            try {ss.close(); } catch (Exception e) { e.printStackTrace();}
        }
    }

    public static String stuLoginCheck(String msg) {
        String message = "";
        try {
            String s = "";
            establishConnection();
            dout.writeUTF(Constant.QUERY_STUDENT + MyConverter.escape(msg));

            int num = din.readInt();
            for (int i = 0; i <= num; i++) {
                s += din.readUTF();
            }
            message = MyConverter.unescape(s);

        } catch (Exception e) {
            e.printStackTrace();
            message = Constant.SERVER_CONNECTION_ERROR;
        } finally {
            closeConnection();
        }

        return message;
    }


    public static String insertSoftWareFeedBack(String msg) {
        String result = "";
        try {
            establishConnection();
            dout.writeUTF(Constant.INSERT_SOFTWARE_FEEDBACK + MyConverter.escape(msg));
            result = MyConverter.unescape(din.readUTF());

        } catch (Exception e) {
            e.printStackTrace();
            result = Constant.SERVER_CONNECTION_ERROR;
        } finally {
            closeConnection();
        }

        return result;
    }


    public static String updateStudent(String msg) {
        String result = "";
        try {
            establishConnection();
            dout.writeUTF(Constant.UPDATE_STUDENT + MyConverter.escape(msg));
            result = MyConverter.unescape(din.readUTF());

        } catch (Exception e) {
            e.printStackTrace();
            result = Constant.SERVER_CONNECTION_ERROR;
        } finally {
            closeConnection();
        }

        return result;
    }

    public static String updateStudentPicture(byte[] bitmap,String sculpture)
    {
        String result="";
        try {
            establishConnection();
            dout.writeUTF(Constant.UPDATE_SCULPTURE+MyConverter.escape(sculpture));
            dout.writeInt(bitmap.length);
            dout.write(bitmap);
            result = Constant.OPERATION_SUCCEED;
        }catch (Exception e){
            result = Constant.SERVER_CONNECTION_ERROR;
            e.printStackTrace();
        }finally {
            closeConnection();
        }
        return  result;
    }

    public static String Online_Class() {
        String result = "";
        String Msg = "";
        try {
            establishConnection();
            dout.writeUTF(Constant.ONLINE_CLASS_CHECK);
            int length = din.readInt();
            for (int i = 0; i <= length; i++) {
                result += din.readUTF();
            }
            Msg = MyConverter.unescape(result);
        } catch (Exception e) {
            e.printStackTrace();
            Msg = Constant.SERVER_CONNECTION_ERROR;
        } finally {
            closeConnection();
        }
        return Msg;
    }

    public static String article() {
        String result = "";
        String Msg = "";
        try {

            establishConnection();
            dout.writeUTF(Constant.ARTICLE_CHECK);
            int length = din.readInt();
            for (int i = 0; i <= length; i++) {
                result += din.readUTF();
            }
            Msg = MyConverter.unescape(result);

        } catch (Exception e) {
            e.printStackTrace();
            Msg = Constant.SERVER_CONNECTION_ERROR;
        } finally {
            closeConnection();
        }
        return Msg;
    }

    public static String queryCourseAndTeacher(String msg) {
        String message = "";
        try {
            String s = "";
            establishConnection();
            dout.writeUTF(Constant.QUERY_COURSE_AND_TEACHER + MyConverter.escape(msg));

            int num = din.readInt();
            for (int i = 0; i <= num; i++) {
                s += din.readUTF();
            }
            message = MyConverter.unescape(s);

        } catch (Exception e) {
            e.printStackTrace();
            message = Constant.SERVER_CONNECTION_ERROR;
        } finally {
            closeConnection();
        }

        return message;
    }

    public static String insertTeacherFeedback(String msg) {
        String result = "";
        try {
            establishConnection();
            dout.writeUTF(Constant.INSERT_TEACHER_FEEDBACK + MyConverter.escape(msg));
            result = MyConverter.unescape(din.readUTF());

        } catch (Exception e) {
            e.printStackTrace();
            result = Constant.SERVER_CONNECTION_ERROR;
        } finally {
            closeConnection();
        }

        return result;
    }

    //question
    public static String queryQuestion(String msg) {
        String result = "";
        String Msg = "";
        try {
            establishConnection();
            dout.writeUTF(Constant.QUERY_QUESTION + MyConverter.escape(msg));

            int length = din.readInt();
            for (int i = 0; i <= length; i++) {
                result += din.readUTF();
            }
            Msg = MyConverter.unescape(result);

        } catch (Exception e) {
            e.printStackTrace();
            Msg = Constant.SERVER_CONNECTION_ERROR;
        } finally {
            closeConnection();
        }

        return Msg;

    }

    public static String queryWrongSet(String msg)
    {
        String message = "";
        try
        {
            String s="";
            establishConnection();
            dout.writeUTF(Constant.QUERY_WRONG_SET+MyConverter.escape(msg));

            int num= din.readInt();
            for(int i=0;i<=num;i++)
            {
                s += din.readUTF();
            }
            message = MyConverter.unescape(s);

        } catch (Exception e) {
            e.printStackTrace();
            message = Constant.SERVER_CONNECTION_ERROR;
        }finally
        {
            closeConnection();
        }

        return message;
    }

    public static String querySelfQuestionComment(String msg) {
        String message = "";
        try {
            String s = "";
            establishConnection();
            dout.writeUTF(Constant.QUERY_SELF_QUESTION_COMMENT + MyConverter.escape(msg));

            int num = din.readInt();
            for (int i = 0; i <= num; i++) {
                s += din.readUTF();
            }
            message = MyConverter.unescape(s);

        } catch (Exception e) {
            e.printStackTrace();
            message = Constant.SERVER_CONNECTION_ERROR;
        } finally {
            closeConnection();
        }

        return message;
    }

    public static String insertQuestionComment(String msg)
    {
        String result = "";
        try {
            establishConnection();
            dout.writeUTF(Constant.INSERT_QUESTION_COMMENT+MyConverter.escape(msg));
            result = MyConverter.unescape(din.readUTF());

        } catch (Exception e) {
            e.printStackTrace();
            result=Constant.SERVER_CONNECTION_ERROR;
        }
        finally{
            closeConnection();
        }

        return result;
    }

    public static String queryQuestionCommentInQuestion(String msg)
    {
        String message = "";
        try
        {
            String s="";
            establishConnection();
            dout.writeUTF(Constant.QUERY_QUESTION_COMMENT_IN_QUESTION+MyConverter.escape(msg));

            int num= din.readInt();
            for(int i=0;i<=num;i++)
            {
                s += din.readUTF();
            }
            message = MyConverter.unescape(s);

        } catch (Exception e) {
            e.printStackTrace();
            message = Constant.SERVER_CONNECTION_ERROR;
        }finally
        {
            closeConnection();
        }

        return message;
    }


    public static String deleteQuestionComment(String msg) {
        String result = "";
        try {
            establishConnection();
            dout.writeUTF(Constant.DELETE_QUESTION_COMMENT + MyConverter.escape(msg));
            result = MyConverter.unescape(din.readUTF());

        } catch (Exception e) {
            e.printStackTrace();
            result = Constant.SERVER_CONNECTION_ERROR;
        } finally {
            closeConnection();
        }

        return result;
    }


    public static byte[] downLoadPicture(String msg) {
        byte[] picData = null;
        try {
            establishConnection();

            dout.writeUTF(Constant.DOWNLOAD_PICTURE + MyConverter.escape(msg));
            picData = IOUtil.readImageBytes(din);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }

        return picData;
    }


    //校园活动
    public static String SchoolCampaign() {
        String result = "";
        String Msg = "";
        try {
            establishConnection();
            dout.writeUTF(Constant.SCHOOL_CAMPAIGN_CHECK);
            int length = din.readInt();
            for (int i = 0; i <= length; i++) {
                result += din.readUTF();
            }
            Msg = MyConverter.unescape(result);
        } catch (Exception e) {
            e.printStackTrace();
            Msg = Constant.SERVER_CONNECTION_ERROR;
        } finally {
            closeConnection();
        }
        return Msg;
    }

    public static String queryTodayCourse(String msg)
    {
        String message = "";
        try
        {
            String s="";
            establishConnection();
            dout.writeUTF(Constant.QUERY_TODAY_COURSE+MyConverter.escape(msg));

            int num= din.readInt();
            for(int i=0;i<=num;i++)
            {
                s += din.readUTF();
            }
            message = MyConverter.unescape(s);

        } catch (Exception e) {
            e.printStackTrace();
            message = Constant.SERVER_CONNECTION_ERROR;
        }finally
        {
            closeConnection();
        }

        return message;
    }

    public static String updateQuizResult(String msg)
    {
        String result = "";
        try {
            establishConnection();
            dout.writeUTF(Constant.UPDATE_QUIZ_RESULT+MyConverter.escape(msg));
            result = MyConverter.unescape(din.readUTF());

        } catch (Exception e) {
            e.printStackTrace();
            result=Constant.SERVER_CONNECTION_ERROR;
        }
        finally{
            closeConnection();
        }

        return result;
    }

    public static String queryQuestionInPackage(String msg)
    {
        String message = "";
        try
        {
            String s="";
            establishConnection();
            dout.writeUTF(Constant.QUERY_QUESTION_IN_PACKAGE+MyConverter.escape(msg));

            int num= din.readInt();
            for(int i=0;i<=num;i++)
            {
                s += din.readUTF();
            }
            message = MyConverter.unescape(s);

        } catch (Exception e) {
            e.printStackTrace();
            message = Constant.SERVER_CONNECTION_ERROR;
        }finally{
            closeConnection();
        }

        return message;
    }

    public static String queryIPAddress(String msg)
    {
        String result = "";
        try {
            establishConnection();
            dout.writeUTF(Constant.QUERY_IP_ADDRESS+MyConverter.escape(msg));
            result = MyConverter.unescape(din.readUTF());

        } catch (Exception e) {
            e.printStackTrace();
            result=Constant.SERVER_CONNECTION_ERROR;
        }
        finally{
            closeConnection();
        }

        return result;
    }

}
