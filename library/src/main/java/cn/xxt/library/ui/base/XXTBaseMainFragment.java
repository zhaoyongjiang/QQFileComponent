package cn.xxt.library.ui.base;

import android.content.Context;

/**
 *
 * 懒加载
 *
 * Created by zyj on 2018/12/30.
 */
public class XXTBaseMainFragment extends XXTSupportFragment {
    protected OnBackToFirstListener _mBackToFirstListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnBackToFirstListener) {
            _mBackToFirstListener = (OnBackToFirstListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnBackToFirstListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        _mBackToFirstListener = null;
    }

    @Override
    public boolean onBackPressedSupport() {
        return super.onBackPressedSupport();
    }

    public interface OnBackToFirstListener {
        void onBackToFirstFragment();
    }
}
