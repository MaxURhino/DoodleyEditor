package net.maxurhino.doodley_editor.util.platform_specific.windows;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public class WindowsDarkTitleBar {
    private static final int DWMWA_USE_IMMERSIVE_DARK_MODE_NEW = 20; // Windows 10 20H1+ / Windows 11
    private static final int DWMWA_USE_IMMERSIVE_DARK_MODE_OLD = 19; // older Windows 10 builds

    public static void enable(long hwnd, boolean dark) {
        try (Arena arena = Arena.ofConfined()) {
            Linker linker = Linker.nativeLinker();
            SymbolLookup dwmapi = SymbolLookup.libraryLookup("dwmapi.dll", arena);

            MethodHandle dwmSetWindowAttribute = linker.downcallHandle(
                    dwmapi.find("DwmSetWindowAttribute").orElseThrow(),
                    FunctionDescriptor.of(
                            ValueLayout.JAVA_INT,   // HRESULT return
                            ValueLayout.ADDRESS,    // HWND
                            ValueLayout.JAVA_INT,   // DWORD dwAttribute
                            ValueLayout.ADDRESS,    // LPCVOID pvAttribute
                            ValueLayout.JAVA_INT    // DWORD cbAttribute
                    )
            );

            MemorySegment hwndSegment = MemorySegment.ofAddress(hwnd);
            MemorySegment value = arena.allocate(ValueLayout.JAVA_INT);
            value.set(ValueLayout.JAVA_INT, 0, dark ? 1 : 0);

            int hr = (int) dwmSetWindowAttribute.invoke(
                    hwndSegment,
                    DWMWA_USE_IMMERSIVE_DARK_MODE_NEW,
                    value,
                    (int) ValueLayout.JAVA_INT.byteSize()
            );

            if (hr != 0) { // S_OK == 0; try the legacy attribute id on failure
                dwmSetWindowAttribute.invoke(
                        hwndSegment,
                        DWMWA_USE_IMMERSIVE_DARK_MODE_OLD,
                        value,
                        (int) ValueLayout.JAVA_INT.byteSize()
                );
            }
        } catch (Throwable t) {
            throw new RuntimeException("Failed to set dark title bar", t);
        }
    }
}