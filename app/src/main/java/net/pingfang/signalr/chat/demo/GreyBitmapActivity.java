package net.pingfang.signalr.chat.demo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.pingfang.signalr.chat.R;
import net.pingfang.signalr.chat.util.CommonTools;

public class GreyBitmapActivity extends AppCompatActivity implements View.OnClickListener{

    // svn提交测试

    TextView btn_activity_back;

    ImageView iv_primary_pic;
    ImageView iv_converted_pic;
    Button btn_convert_gray_pic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grey_bitmap);

        initView();
    }

    private void initView() {

        btn_activity_back = (TextView) findViewById(R.id.btn_activity_back);
        btn_activity_back.setOnClickListener(this);

        iv_primary_pic = (ImageView) findViewById(R.id.iv_primary_pic);
        iv_converted_pic = (ImageView) findViewById(R.id.iv_converted_pic);
        btn_convert_gray_pic = (Button) findViewById(R.id.btn_convert_gray_pic);
        btn_convert_gray_pic.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch(viewId) {
            case R.id.btn_activity_back:
                navigateUp();
                break;
            case R.id.btn_convert_gray_pic:
                convertPic();
                break;
        }
    }

    private void convertPic() {
        Drawable primaryDrawable = iv_primary_pic.getDrawable();
        if(primaryDrawable != null && primaryDrawable instanceof BitmapDrawable) {
            Bitmap primaryBitmap = ((BitmapDrawable) primaryDrawable).getBitmap();
            iv_converted_pic.setImageBitmap(CommonTools.convertToGrayscale(primaryBitmap));
        }
    }

    public void navigateUp() {
        Intent upIntent = NavUtils.getParentActivityIntent(this);
        if(NavUtils.shouldUpRecreateTask(this, upIntent)) {
            TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(upIntent)
                    .startActivities();
        } else {
            onBackPressed();
        }
    }
}
