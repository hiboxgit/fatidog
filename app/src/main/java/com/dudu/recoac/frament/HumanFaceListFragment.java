package com.dudu.recoac.frament;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.dudu.fatidog.R;
import com.dudu.fatidog.bean.UpdateFaceListEvent;
import com.dudu.recoac.adapter.FaceAdapter;
import com.dudu.recoac.bean.FaceRecogPersonData;
import com.dudu.recoac.core.FaceRecogValueHelper;
import com.dudu.recoac.core.FaceRecogWorkFlow;
import com.dudu.recoac.dialog.InputFaceNameDialog;
import com.dudu.recoac.utils.DialogUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * @author luo zha
 * @CreateDate 2017-02-05 17:08.
 */
public class HumanFaceListFragment extends BaseFragment implements View.OnClickListener, FaceAdapter.OnViewListener, AdapterView.OnItemClickListener {
    private Logger logger = LoggerFactory.getLogger("FatiDog.app.recoac.HumanFaceListFragment");
    private TextView mEditFaceTV;
    private ListView mFaceList;
    private List<FaceRecogPersonData> mFaceData = null;//= new ArrayList<>();
    private FaceAdapter mAdapter;

    @Override
    protected int initLayoutId() {
        return R.layout.fragment_human_face_list;
    }

    @Override
    protected void initView(View rootView) {
        mEditFaceTV = (TextView) rootView.findViewById(R.id.face_list_edit_tv);
        mFaceList = (ListView) rootView.findViewById(R.id.human_face_list);

        mFaceData = FaceRecogValueHelper.getInstance().getRegisteredFacePersonsCopy();
        mAdapter = new FaceAdapter(mFaceData, getActivity());
        mFaceList.setAdapter(mAdapter);

    }

    @Override
    public void onResume() {
        super.onResume();

        logger.debug("onResume");
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        logger.debug("onPause");
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void initData() {
        loadHumanFaceData();
    }


    @Override
    protected void initListener() {
        mEditFaceTV.setOnClickListener(this);
        mAdapter.setListener(this);
        mFaceList.setOnItemClickListener(this);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            //相当于Fragment的onResume
            logger.info("显示白名单人员列表.");
            loadHumanFaceData();
        } else {
            //相当于Fragment的onPause
            logger.info("隐藏白名单人员列表.");
        }
    }

    private void loadHumanFaceData() {
        logger.info("加载白名单人员列表");
        FaceRecogValueHelper.getInstance().updateRegFacedlist();
        mFaceData = FaceRecogValueHelper.getInstance().getRegisteredFacePersonsCopy();
        if (mFaceData.size() > 0) {
            mAdapter.setData(mFaceData);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.face_list_edit_tv:
                if (mAdapter.getEditStatus() == FaceAdapter.EDIT_STATUS) {
                    mAdapter.editFace();
                    mEditFaceTV.setText("取消");
                } else if (mAdapter.getEditStatus() == FaceAdapter.CANCEL_STATUS) {
                    mAdapter.cancelEditFace();
                    mEditFaceTV.setText("编辑");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onViewClick(View view, int position) {
        logger.info("点击删除的按钮，开始删除第{}个",position);
        if(mFaceData != null){
            if(!mFaceData.isEmpty()){
                //先从白名单中删除
                long personId = mFaceData.get(position).getPersonId();
                FaceRecogWorkFlow.getInstance().cleanPersonFace(personId);
            }else {
                logger.error("mFaceData is Empty");
            }
        }else {
            logger.error("mFaceData is null");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (mAdapter.getEditStatus() == FaceAdapter.EDIT_STATUS) {
            DialogUtils.showInputFaceNameDialog(getActivity(), mFaceData.get(i).getName(), new InputFaceNameDialog.OnViewClickListener() {
                @Override
                public void onConfirmView(View view, String faceName) {
                    logger.error("点击了确认的按钮:{}",faceName);
                    DialogUtils.dismissInputFaceNameDialog();

                    logger.info("更新注册人脸人员名称");
                    FaceRecogValueHelper.getInstance().setRegPersonName(i,faceName);

                    mAdapter.setData(mFaceData);
                }

                @Override
                public void onCancelView(View view, String faceName) {
                    Log.v("luo", "点击了取消的按钮:" + faceName);
                    DialogUtils.dismissInputFaceNameDialog();
                }
            });
        }
    }

    public void onEventMainThread(UpdateFaceListEvent event){
        logger.info("接收到刷新列表消息通知...");
        mFaceData = FaceRecogValueHelper.getInstance().getRegisteredFacePersonsCopy();
        mAdapter.setData(mFaceData);
    }
}
