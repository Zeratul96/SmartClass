package com.bn.main;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bn.parameter.HandlerConstant;
import com.bn.parameter.Preference;
import com.bn.person.MyCommentActivity;
import com.bn.person.MyTeacherActivity;
import com.bn.person.NotesListActivity;
import com.bn.person.PersonCenterActivity;
import com.bn.person.PersonalSculptureActivity;
import com.bn.settings.SettingsActivity;
import com.bn.smartclass_android.R;
import com.bn.tool_package.ImageTools;
import com.bn.util.NetConnectionUtil;


/**
 * Created by 13273 on 2017/7/22.
 *
 */

public class PersonFragment extends Fragment implements OnClickListener
{
    ImageView sculpture;
    TextView tx;
    static boolean needReload;
    Bitmap userImg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.person_layout, container, false);

        //将五个选项全部添加监听
        view.findViewById(R.id.personal_center).setOnClickListener(this);
        view.findViewById(R.id.notes).setOnClickListener(this);
        view.findViewById(R.id.teacher).setOnClickListener(this);
        view.findViewById(R.id.comment).setOnClickListener(this);
        view.findViewById(R.id.settings).setOnClickListener(this);


        tx = (TextView)view.findViewById(R.id.personName);
        sculpture = (ImageView) view.findViewById(R.id.sculpture);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        tx.setText(Preference.dataInfoList.get(6));
        initPersonalSculpture();
        needReload = false;
        System.gc();
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.personal_center:
                startActivity(new Intent(MainActivity.mainActivity, PersonCenterActivity.class));
                break;


            case R.id.notes:
                startActivity(new Intent(MainActivity.mainActivity, NotesListActivity.class));
                break;


            case R.id.teacher:
                startActivity(new Intent(MainActivity.mainActivity, MyTeacherActivity.class));
                break;


            case R.id.comment:
                startActivity(new Intent(MainActivity.mainActivity, MyCommentActivity.class));
                break;


            case R.id.settings:
                startActivity(new Intent(MainActivity.mainActivity, SettingsActivity.class));
                break;
        }

    }

    /**
     * 加载个人头像至内存
     */
    private void initPersonalSculpture()
    {
        if(Preference.dataInfoList.get(9).equals("未填写"))
        {
            Resources res = getResources();
            Bitmap picSclputure = BitmapFactory.decodeResource(res,R.drawable.login_sculpture);
            sculpture.setImageBitmap(ImageTools.createCircleImage(picSclputure));
            return;
        }


        //如果能在本地获取图片则本地获取
        if(ImageTools.filesExists(Preference.appDataPath+ Preference.sculptureFile))
        {
            sculpture.setImageBitmap(ImageTools.createCircleImage(ImageTools.getLocalBitmap(Preference.sculptureFile)));
        }

        else//连接服务器下载头像
        {
            sculpture.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.color.no_picture));
            final Handler myHandler = new Handler()
            {
                @Override
                public void handleMessage(Message msg)
                {
                    super.handleMessage(msg);

                    switch (msg.what)
                    {
                        case HandlerConstant.CONNECTION_FAILURE:
                            needReload = true;
                            Preference.downloadBitmapFailure = true;
                            break;

                        case HandlerConstant.OPERATION_SUCCEED:
                            Preference.downloadBitmapFailure = false;
                            sculpture.setImageBitmap(userImg);
                            break;
                    }
                }
            };

            new Thread(new Runnable() {
                @Override
                public void run()
                {
                    byte[] picData = NetConnectionUtil.downLoadPicture(Preference.dataInfoList.get(9));
                    Message message = new Message();
                    if(picData!=null)
                    {
                        userImg = ImageTools.getBitmapFromByteArray(picData);
                        ImageTools.setLocalBitmap(picData, Preference.sculptureFile);
                        message.what = HandlerConstant.OPERATION_SUCCEED;
                    }
                    else
                        message.what = HandlerConstant.CONNECTION_FAILURE;

                    myHandler.sendMessage(message);
                }
            }).start();
        }
    }
}
