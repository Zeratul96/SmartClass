package com.bn.parameter;

/**
 * Created by 13273 on 2017/7/24.
 *
 */

public class Constant {

    public static final String SERVER_CONNECTION_ERROR = "<#ServerConnectionError#>";//服务器连接失败

    //操作提示
    public static final String OPERATION_SUCCEED = "<#OperationSucceed#>";
    public static final String NO_RECORD_FOUND = "<#NoRecordFound#>";

    //Student
    public static final String QUERY_STUDENT = "<#QueryStudent#>";
    public static final String UPDATE_STUDENT = "<#UpdateStudent#>";
    public static final String QUERY_COURSE_AND_TEACHER = "<#QueryCourseAndTeacher#>";
    public static final String UPDATE_SCULPTURE = "<#UpdateSculpture#>";

    //teacher_comment
    public static final String INSERT_TEACHER_FEEDBACK = "<#InsertTeacherFeedback#>";

    //Feedback
    public static final String INSERT_SOFTWARE_FEEDBACK = "<#InsertSoftwareFeedback#>";

    //OnlineClass//Article//SchoolCampaign
    public static final String ONLINE_CLASS_CHECK="<#OnlineClassCheck#>";
    public static final String ARTICLE_CHECK="<#Article#>";
    public static final String SCHOOL_CAMPAIGN_CHECK="<#SchoolCampaignCheck#>";

    //questionComment
    public static final String QUERY_SELF_QUESTION_COMMENT = "<#QuerySelfQuestionComment#>";
    public static final String DELETE_QUESTION_COMMENT = "<#DeleteQuestionComment#>";
    public static final String INSERT_QUESTION_COMMENT = "<#InsertQuestionComment#>";
    public static final String QUERY_QUESTION_COMMENT_IN_QUESTION = "<#QueryQuestionCommentInQuestion#>";

    //下载图片
    public static final String DOWNLOAD_PICTURE = "<#DownloadPicture#>";

    //question
    public static final String QUERY_QUESTION = "<#QueryQustion#>";
    public static final String QUERY_WRONG_SET = "<#QueryWrongSet#>";

    //student_timetable
    public static final String QUERY_TODAY_COURSE = "<#QueryTodayCourse#>";

    //QuizResult
    public static final String UPDATE_QUIZ_RESULT = "<#UpdateQuizResult#>";

    //关于上课直播
    public static final String QUERY_IP_ADDRESS = "<#QueryIPAddress#>";
    public static final int MODE_PPT = 0;
    public static final int MODE_QUESTION = 1;
    public static String TEACHER_IP="";
    public static int BROADCAST_MODE=2;


    //PersonCenter
    public static final int TAKE_PHOTO = 0;//拍照
    public static final int SELECT_PICTURE = 1;//选择相册的图片
    public static final int CANCEL = 2;//取消
    public static final int CROP_SMALL_PICTURE=3;//裁剪图片

    //本地数据库
    public static final int NOTES = 0;
    public static final int WRONG_SET = 1;

    //Question_In_Package
    public static final String QUERY_QUESTION_IN_PACKAGE = "<#QueryQuestionInPackage#>";
}
