package pub.solare.hotfixdemo;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public final class HotFix {
    /**
     * 修复指定的类
     *
     * @param context        上下文对象
     * @param fixDexFilePath   修复的dex文件路径，包含dex文件名
     */
    public static void fixDexFile(Context context, String fixDexFilePath) {
        if (fixDexFilePath != null && new File(fixDexFilePath).exists()) {
            try {
                injectDexToClassLoader(context, fixDexFilePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param context
     * @param fixDexFilePath 修复文件的路径
     * @throws ClassNotFoundException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private static void injectDexToClassLoader(Context context, String fixDexFilePath)
            throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {

        // 这里获取了原有的dex列表
        PathClassLoader pathClassLoader = (PathClassLoader) context.getClassLoader();
        Object basePathList = getPathList(pathClassLoader);
        // 从PathList里获取dexElements数组
        Object baseElements = getDexElements(basePathList);

        //读取 fixElements
        String baseDexAbsolutePath = context.getDir("dex", 0).getAbsolutePath();
        // 实例化一个DexClassLoader
        DexClassLoader fixDexClassLoader = new DexClassLoader(
                fixDexFilePath, baseDexAbsolutePath, fixDexFilePath, context.getClassLoader());
        // 获取补丁包里的dexElements列表
        Object fixPathList = getPathList(fixDexClassLoader);
        Object fixElements = getDexElements(fixPathList);

        //合并两份Elements
        Object newElements = combineArray(baseElements, fixElements);

        //一定要重新获取，不要用basePathList，会报错
        Object basePathList2 = getPathList(pathClassLoader);

        //新的dexElements对象重新设置回去
        setField(basePathList2, basePathList2.getClass(), "dexElements", newElements);
        Log.v("Solare", "Fixed");
    }

    /**
     * 通过反射先获取到pathList对象
     *
     * @param obj
     * @return
     * @throws ClassNotFoundException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private static Object getPathList(Object obj) throws ClassNotFoundException, NoSuchFieldException,
            IllegalAccessException {
        return getField(obj, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    /**
     * 从上面获取到的PathList对象中，进一步反射获得dexElements对象
     *
     * @param obj
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private static Object getDexElements(Object obj) throws NoSuchFieldException, IllegalAccessException {
        return getField(obj, obj.getClass(), "dexElements");
    }

    // 通过反射从一个类里获取成员变量，str是成员变量的名字
    // 把返回这个成员变量的值
    private static Object getField(Object obj, Class cls, String str)
            throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = cls.getDeclaredField(str);
        declaredField.setAccessible(true);//设置为可访问
        return declaredField.get(obj);
    }

    private static void setField(Object obj, Class cls, String str, Object obj2)
            throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = cls.getDeclaredField(str);
        declaredField.setAccessible(true);//设置为可访问
        declaredField.set(obj, obj2);
    }

    /**
     * 合拼dexElements ,并确保 fixElements 在 baseElements 之前
     *
     * @param baseElements
     * @param fixElements
     * @return
     */
    private static Object combineArray(Object baseElements, Object fixElements) {
        Class componentType = fixElements.getClass().getComponentType();
        int length = Array.getLength(fixElements);
        int length2 = Array.getLength(baseElements) + length;
        Object newInstance = Array.newInstance(componentType, length2);
        for (int i = 0; i < length2; i++) {
            if (i < length) {
                Array.set(newInstance, i, Array.get(fixElements, i));
            } else {
                Array.set(newInstance, i, Array.get(baseElements, i - length));
            }
        }
        return newInstance;
    }
}