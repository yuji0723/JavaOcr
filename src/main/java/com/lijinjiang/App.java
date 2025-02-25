package com.lijinjiang;

import com.lijinjiang.beautyeye.BeautyEyeLNFHelper;
import com.lijinjiang.view.MainFrame;

public class App {
    public static void main(String[] args) {
        try {
            /**
             * 设置本属性将改变窗口边框样式定义
             * 系统默认样式 : osLookAndFeelDecorated
             * 强立体半透明 : translucencyAppleLike
             * 弱立体半透明 : translucencySmallShadow
             * 普通不透明 : generalNoTranslucencyShadow
             */
            BeautyEyeLNFHelper.frameBorderStyle = BeautyEyeLNFHelper.FrameBorderStyle.generalNoTranslucencyShadow;
            BeautyEyeLNFHelper.launchBeautyEyeLNF();
            // 初始化主窗口
            new MainFrame();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}
