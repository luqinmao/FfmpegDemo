package com.lansoeditor.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lansoeditor.demo.util.CustomPopWindow;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * user：lqm
 * desc：社区界面
 */

public class CommunityActivity extends Activity {

    @InjectView(R.id.tv_to_progress)
    TextView tvToProgress;
    @InjectView(R.id.rv_community)
    RecyclerView rvCommunity;
    @InjectView(R.id.btn_add)
    Button btnAdd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_community);
        ButterKnife.inject(this);

        initView();

    }

    private void initView() {

    }

    @OnClick({R.id.tv_to_progress, R.id.btn_add})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_to_progress:
                startActivity(new Intent(CommunityActivity.this,ProgressActivity.class));
                break;
            case R.id.btn_add:
                showAddPopup();
                break;
        }
    }

    private void showAddPopup() {

        final View shareView = View.inflate(CommunityActivity.this, R.layout.popup_post_community, null);

        final CustomPopWindow popWindow = new CustomPopWindow.PopupWindowBuilder(this)
                .setView(shareView)
                .setAnimationStyle(R.style.popwindow_anim)
                .create();
        popWindow.showAsDropDown(btnAdd,-200,  - (btnAdd.getHeight() + popWindow.getHeight()));

//        final ViewGroup.LayoutParams layoutParams = shareView.getLayoutParams();
//        ValueAnimator animator =ValueAnimator.ofInt(0,popWindow.getHeight());
//        animator.setDuration(1000);
//        animator.start();
//        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                int value = (int) valueAnimator.getAnimatedValue();
//                layoutParams.height = value;
//                shareView.setLayoutParams(layoutParams);
//
//            }
//        });

    }
}
