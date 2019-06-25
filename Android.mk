#ifeq ($(strip $(ZF_TTS_FACTORY_SUPPORT)),yes)
	LOCAL_ROOT_PATH:= $(call my-dir)
	include $(LOCAL_ROOT_PATH)/app/Android.mk
#endif


