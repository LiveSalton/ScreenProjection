package com.salton123.soulove;

import android.content.Context;

import com.hjq.toast.ToastUtils;
import com.salton123.app.BaseApplication;
import com.salton123.soulove.common.AppHelper;
import com.salton123.soulove.common.BuildConfig;
import com.salton123.soulove.common.widget.TRefreshHeader;
import com.salton123.soulove.sdk.ThirdHelper;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.ximalaya.ting.android.opensdk.util.BaseUtil;

import androidx.multidex.MultiDex;

/**
 * User: newSalton@outlook.com
 * Date: 2019/5/23 15:40
 * ModifyTime: 15:40
 * Description:
 */
public class XApp extends BaseApplication {

    static {
        //设置全局默认配置（优先级最低，会被其他设置覆盖）
        SmartRefreshLayout.setDefaultRefreshInitializer((context, layout) -> {
            //开始设置全局的基本参数（可以被下面的DefaultRefreshHeaderCreator覆盖）
            layout.setHeaderMaxDragRate(1.5f);
        });
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator((context, layout) -> new TRefreshHeader(context));
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator((context, layout) -> {
            ClassicsFooter classicsFooter = new ClassicsFooter(context);
            classicsFooter.setTextSizeTitle(12);
            classicsFooter.setDrawableSize(16);
            classicsFooter.setFinishDuration(0);
            return classicsFooter;
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ToastUtils.init(this);
        if (BaseUtil.isMainProcess(this)) {
            ThirdHelper.getInstance(this)
                    .initQualityAssistant()
                    .initFragmentation(BuildConfig.APP_DEVELOP)
                    .initRouter()
                    .initUtils()
                    .initBugly()
                    .initCrashView();
            AppHelper.getInstance(this)
                    .initXmly();
        }
    }

}
