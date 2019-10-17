package com.genymobile.scrcpy.wrappers;

import com.genymobile.scrcpy.Ln;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Build;
import android.os.IBinder;
import android.view.Surface;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressLint("PrivateApi")
public final class SurfaceControl {

    private static final Class<?> CLASS;

    // see <https://android.googlesource.com/platform/frameworks/base.git/+/pie-release-2/core/java/android/view/SurfaceControl.java#305>
    public static final int POWER_MODE_OFF = 0;
    public static final int POWER_MODE_NORMAL = 2;

    static {
        try {
            CLASS = Class.forName("android.view.SurfaceControl");
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    private static Method getBuiltInDisplayMethod;
    private static Method setDisplayPowerModeMethod;

    private SurfaceControl() {
        // only static methods
    }

    public static void openTransaction() {
        try {
            CLASS.getMethod("openTransaction").invoke(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static void closeTransaction() {
        try {
            CLASS.getMethod("closeTransaction").invoke(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static void setDisplayProjection(IBinder displayToken, int orientation, Rect layerStackRect, Rect displayRect) {
        try {
            CLASS.getMethod("setDisplayProjection", IBinder.class, int.class, Rect.class, Rect.class)
                    .invoke(null, displayToken, orientation, layerStackRect, displayRect);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static void setDisplayLayerStack(IBinder displayToken, int layerStack) {
        try {
            CLASS.getMethod("setDisplayLayerStack", IBinder.class, int.class).invoke(null, displayToken, layerStack);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static void setDisplaySurface(IBinder displayToken, Surface surface) {
        try {
            CLASS.getMethod("setDisplaySurface", IBinder.class, Surface.class).invoke(null, displayToken, surface);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static IBinder createDisplay(String name, boolean secure) {
        try {
            return (IBinder) CLASS.getMethod("createDisplay", String.class, boolean.class).invoke(null, name, secure);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static Method getGetBuiltInDisplayMethod() {
        if (getBuiltInDisplayMethod == null) {
            try {
                // the method signature has changed in Android Q
                // <https://github.com/Genymobile/scrcpy/issues/586>
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    getBuiltInDisplayMethod = CLASS.getMethod("getBuiltInDisplay", int.class);
                } else {
                    getBuiltInDisplayMethod = CLASS.getMethod("getInternalDisplayToken");
                }
            } catch (NoSuchMethodException e) {
                Ln.e("Could not find method", e);
            }
        }
        return getBuiltInDisplayMethod;
    }

    public static IBinder getBuiltInDisplay() {
        Method method = getGetBuiltInDisplayMethod();
        if (method == null) {
            return null;
        }
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                // call getBuiltInDisplay(0)
                return (IBinder) method.invoke(null, 0);
            }

            // call getInternalDisplayToken()
            return (IBinder) method.invoke(null);
        } catch (InvocationTargetException | IllegalAccessException e) {
            Ln.e("Could not invoke " + method.getName(), e);
            return null;
        }
    }

    private static Method getSetDisplayPowerModeMethod() {
        if (setDisplayPowerModeMethod == null) {
            try {
                setDisplayPowerModeMethod = CLASS.getMethod("setDisplayPowerMode", IBinder.class, int.class);
            } catch (NoSuchMethodException e) {
                Ln.e("Could not find method", e);
            }
        }
        return setDisplayPowerModeMethod;
    }

    public static void setDisplayPowerMode(IBinder displayToken, int mode) {
        Method method = getSetDisplayPowerModeMethod();
        if (method == null) {
            return;
        }
        try {
            method.invoke(null, displayToken, mode);
        } catch (InvocationTargetException | IllegalAccessException e) {
            Ln.e("Could not invoke " + method.getName(), e);
        }
    }

    public static void destroyDisplay(IBinder displayToken) {
        try {
            CLASS.getMethod("destroyDisplay", IBinder.class).invoke(null, displayToken);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
