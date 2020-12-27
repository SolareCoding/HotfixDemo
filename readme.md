## 如何使用

修改代码后，手动使用build-tools里的dx工具将修改类对应的class文件转成dex文件。（本项目里已经放好了一个classes2.dex文件）
把修复包放到Environment.getExternalStorageDirectory()对应的文件夹下。
App中直接点击Show会显示Bug。
重启App点击Fix再点击Show显示Bug修复。

Android10以上运行无效。

## 原理
类替换。将修复包中的类加载出来，放到dexElements首部。这样在加载错误类时，首先会加载到修复包里的类。