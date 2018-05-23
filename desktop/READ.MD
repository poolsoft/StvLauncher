#目录说明
desktop_app       应用桌面
desktop_example   Demo桌面
desktop_leanback  Leanback桌面，目前库冲突，不参与编译，可参考它的源码

#如何添加一个桌面
1.New module，拖拽到desktop目录中
2.根目录下的settings.gradle，指定新增的module
3.根目录/app/build.gradle中的dependencies，指定编译新增的module

