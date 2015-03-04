package com.bruce.animationwithfragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

/**
 * Created by bruce on 2015/3/4.
 */
public class AnimateDemoActivity extends Activity {

    private RelativeLayout mMainContainer; //主布局容器
    private FrameLayout mEditModeContainer;  //编辑模块布局容器
    private FrameLayout mEditFragmentContainer; //编辑模块fragment布局容器
    private View localfrom, localto, datefrom, dateto; //停留在顶部的view
    private LinearLayout mFirstGroup, mSecondGroup, mThirdGroup; //主布局分割的模块
    private boolean isAniamted; //二次点击的时候执行反向动画，避免视图错乱
    private Button mSearch;
    private View mFirstSpacer; //第一个空格view
    private ActionMode mActionMode; //临时actionBar菜单栏
    private int mHalfHeight; //视图布局的一半高度
    private CustomAnimator animator = new CustomAnimator(); //视图动画类
    private TextView mLocalFrom;
    private TextView mLocalTo;
    private TextView mDateFrom;
    private TextView mDateTo;
    private TextView mFinalVisibleView; //操作后停留在顶部的view


    //编辑模块视图
    private TextView mToday;
    private TextView mTomorrow;
    private TextView mThird;


    /**
     * 临时占据AcitonBar的位置，但是与ActionBar是独立存在的，为Context Menu的一种
     */
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        //actionBar被创建的时候调用 也就是调用 startActionMode()
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate menu的资源文件
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.action_mode, menu);
            return true;
        }

        //在actionBar显示的时候调用，通常在onCreateActionMode后面执行，
        // 如果actionBar被重绘，可能导致多次调用
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false 默认什么都不做
        }

        // 选择actionBar的item时调用
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.done:
                    animator.prepareRevert();
                    animator.start();
                    isAniamted = !isAniamted;
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        // 用户点击左上角勾勾时
        //如果想去除左上角的勾勾

        /* <style name="AppTheme" parent="android:Theme.Holo">
               <item name="android:actionModeCloseButtonStyle">@style/NoCloseButton</item>
           </style>

            <style name="NoCloseButton" parent="@android:style/Widget.ActionButton.CloseMode">
                <item name="android:visibility">gone</item>
             </style>
         */
        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final View view = getLayoutInflater().inflate(R.layout.animate_fragmet, null);
        /**
         * 此方法必须加上，因为在布局变化的时候，需要重新计算动画执行开始的高度
         */
        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);
               // mHalfHeight = view.getHeight() / 2;
               // mEditModeContainer.setTranslationY(mHalfHeight);
                mEditModeContainer.setTranslationY(view.getHeight());
                mEditModeContainer.setAlpha(0f);
                //设置编辑界面动画开始执行的高度，这里是整个界面一半的位置，可以将值设置为view.getHeight()做对比可看出效果
                //设置为一半是会导致界面的点击的冲突
                animator.setEditModeHalfHeight(view.getHeight());
                //动画执行时间
                animator.setDuration(500);
            }
        });

        setContentView(view);
        retrieveViews();
    }

    /**
     * 设置页面的view值
     */
    private void retrieveViews() {
        mMainContainer = (RelativeLayout) findViewById(R.id.main_container);

        //编辑框
        mLocalFrom = (TextView) findViewById(R.id.text_localfrom);
        mLocalTo = (TextView) findViewById(R.id.text_localto);
        mDateFrom = (TextView) findViewById(R.id.text_datefrom);
        mDateTo = (TextView) findViewById(R.id.text_dateto);

        //搜索
        mSearch = (Button) findViewById(R.id.search);

        //整体布局分为三块
        mFirstGroup = (LinearLayout) findViewById(R.id.first_group_container);
        mSecondGroup = (LinearLayout) findViewById(R.id.second_group_container);
        mThirdGroup = (LinearLayout) findViewById(R.id.third_group_container);

        //需要最后停留在顶部的view
        localfrom = findViewById(R.id.localfrom);
        localto = findViewById(R.id.localto);
        datefrom = findViewById(R.id.datefrom);
        dateto = findViewById(R.id.dateto);

        //编辑模块的布局
        mEditModeContainer = (FrameLayout) findViewById(R.id.edit_mode_container);
        //编辑模块fragment的布局
        mEditFragmentContainer = (FrameLayout) findViewById(R.id.edit_mode_fragment_container);

        //在此处编辑模块的视图是在布局中写死了，实际中应该使用不同的布局，mEditFragmentContainer.addView();
        //这样动态加入布局的方式来加载不同的布局
        handleEditFragmentView();

        //第一个分割线
        mFirstSpacer = findViewById(R.id.first_spacer);

        handleClick();
    }

    /**
     * 处理 编辑模块的视图
     */
    private void handleEditFragmentView() {
        mToday = (TextView) findViewById(R.id.today);
        mTomorrow = (TextView) findViewById(R.id.tomorrow);
        mThird = (TextView) findViewById(R.id.third);

        mToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFinalVisibleView.setText(mToday.getText());
                hideEditMode();
            }
        });

        mTomorrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFinalVisibleView.setText(mTomorrow.getText());
                hideEditMode();
            }
        });

        mThird.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFinalVisibleView.setText(mThird.getText());
                hideEditMode();
            }
        });
    }

    /**
     * 处理可停留顶部view的点击事件
     */
    private void handleClick() {
        /**
         *  setAnimatorViews 的个参数
         * @param mainContainer
         * @param focusedView
         * @param focusedViewContainer
         * @param fadedOutToBottomViews
         * @param stickyTo
         * @param editModeView
         * @param slideToTop
         * 主要是fadedOutToBottomViews slideToTop这两个集合中view的处理
         * 如果想让效果更佳，需要把主布局分为很多块，然后每个块都是一个布局，
         * 分的越细，效果越佳
         */
        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AnimateDemoActivity.this, "Searching", Toast.LENGTH_SHORT).show();
            }
        });

        localfrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                animator.setAnimatorViews(mMainContainer, localfrom, mFirstGroup,
                        Arrays.asList(new View[]{mSecondGroup, mFirstSpacer, mThirdGroup}), null, mEditModeContainer, Arrays.asList(new View[]{}));
                if (!isAniamted) {
                    showEditMode();
                    mFinalVisibleView = mLocalFrom;
                } else {
                    hideEditMode();
                }
            }
        });

        localto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animator.setAnimatorViews(mMainContainer, localto, mFirstGroup,
                        Arrays.asList(new View[]{mSecondGroup, mFirstSpacer, mThirdGroup}), null, mEditModeContainer, Arrays.asList(new View[]{}));
                if (!isAniamted) {
                    showEditMode();
                    mFinalVisibleView = mLocalTo;
                } else {
                    hideEditMode();
                }
            }
        });

        datefrom.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //animator.setAnimatorViews(mMainContainer, mTv2, mFirstGroup, Arrays.asList(new View[]{mSecondGroup, mSecondSpacer}), mFirstSpacer, mEditModeContainer, Arrays.asList(new View[]{}));
                animator.setAnimatorViews(mMainContainer, datefrom, mSecondGroup, Arrays.asList(new View[]{mThirdGroup}), null, mEditModeContainer, Arrays.asList(new View[]{mFirstGroup, mFirstSpacer}));
                if (!isAniamted) {
                    showEditMode();
                    mFinalVisibleView = mDateFrom;
                } else {
                    hideEditMode();
                }
            }
        });
        dateto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //animator.setAnimatorViews(mMainContainer, mTv2, mFirstGroup, Arrays.asList(new View[]{mSecondGroup, mSecondSpacer}), mFirstSpacer, mEditModeContainer, Arrays.asList(new View[]{}));
                animator.setAnimatorViews(mMainContainer, dateto, mSecondGroup, Arrays.asList(new View[]{mThirdGroup}), null, mEditModeContainer, Arrays.asList(new View[]{mFirstGroup, mFirstSpacer}));
                if (!isAniamted) {
                    showEditMode();
                    mFinalVisibleView = mDateTo;
                } else {
                    hideEditMode();
                }
            }
        });
    }

    /**
     * 显示编辑模块时的逻辑处理
     */
    private void showEditMode() {
        mActionMode = startActionMode(mActionModeCallback);
        animator.prepareAnimation();
        animator.start();
        isAniamted = !isAniamted;
    }

    /**
     * 隐藏编辑模块时的逻辑处理
     */
    private void hideEditMode() {
        animator.prepareRevert();
        animator.start();
        isAniamted = !isAniamted;
        if (mActionMode != null)
            mActionMode.finish();
    }
}
