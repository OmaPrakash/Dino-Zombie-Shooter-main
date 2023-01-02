APP_OPTIM := release
NDK_TOOLCHAIN_VERSION := clang
APP_STL      := c++_static
APP_CPPFLAGS := -frtti -fexceptions -Wformat=0 -std=c++11 -DGOOGLE_PLAY_STORE -fsigned-char -Wno-nonportable-include-path
APP_LDFLAGS := -latomic
APP_ABI := armeabi-v7a arm64-v8a
APP_PLATFORM := android-14
