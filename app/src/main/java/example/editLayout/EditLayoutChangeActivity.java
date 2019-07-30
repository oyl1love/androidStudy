package example.editLayout;

import android.content.Intent;
import android.os.Bundle;

import com.style.base.activity.BaseFullScreenStableTitleBarActivity;
import com.style.framework.R;
import com.style.framework.databinding.EditLayoutMainActivityBinding;

import org.jetbrains.annotations.Nullable;

public class EditLayoutChangeActivity extends BaseFullScreenStableTitleBarActivity {
    EditLayoutMainActivityBinding bd;

    @Override
    public boolean isLightStatusBar() {
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.edit_layout_main_activity);
        bd = getBinding();
        setToolbarTitle("编辑布局调整");
        bd.viewCustomColor.setOnClickListener(v -> startActivity(new Intent(getContext(), MoveEditLayoutToTopActivity.class)));
        bd.viewTranslucentColor.setOnClickListener(v -> startActivity(new Intent(getContext(), HideStatusBarBottomEditLayoutActivity.class)));
        bd.viewTransparentColor.setOnClickListener(v -> startActivity(new Intent(getContext(), FullScreenBottomEditLayoutActivity.class)));

    }

}