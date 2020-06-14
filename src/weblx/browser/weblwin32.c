#include "weblx_browser_Win32.h"

#include <windows.h>
#include <ddeml.h>
#include <shellapi.h>

DWORD dwidInst = 0UL;

HDDEDATA CALLBACK LocationDdeCallBack(UINT type, UINT fmt,
	HCONV hconv, HSZ hsz1, HSZ hsz2, HDDEDATA hData, DWORD dwData1,
	DWORD dwData2)	{

	// Being a simple client application not taking care of any transaction.
	// simply return NULL

	return (HDDEDATA) NULL;
}

JNIEXPORT jstring JNICALL Java_weblx_browser_Win32_DDERequest
  (JNIEnv *env, jobject clazz, jstring app, jstring topic, jstring item)
{
	HCONV hConv = NULL;
	DWORD dderesult;
    const char *sapp = (*env)->GetStringUTFChars(env, app, (jboolean *)NULL);
    const char *stopic = (*env)->GetStringUTFChars(env, topic, (jboolean *)NULL);
    const char *sitem = (*env)->GetStringUTFChars(env, item, (jboolean *)NULL);
	jstring result = (*env)->NewStringUTF(env, "");

	if(dwidInst == 0UL)
		DdeInitialize(&dwidInst, LocationDdeCallBack, APPCLASS_STANDARD | APPCMD_CLIENTONLY, 0ul);

	if (dwidInst == 0UL) {
		jclass ex = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
		if (ex != 0)
			(*env)->ThrowNew(env, ex, "could not  initialize DDE");
	} else {
		//Connect to the server.
		HSZ hszService = DdeCreateStringHandle(dwidInst, sapp, CP_WINANSI);
		HSZ hszTopic = DdeCreateStringHandle(dwidInst, stopic, CP_WINANSI);
		hConv = DdeConnect(dwidInst, hszService, hszTopic, NULL);

		// Check for the connection
		if(hConv == 0UL){
			jclass ex = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
			if (ex != 0)
				(*env)->ThrowNew(env, ex, "could not not establish DDE connection");
		} else {
			//Convert the item into string handle and pass it to server.
			HSZ hszItem = DdeCreateStringHandle(dwidInst, sitem, CP_WINANSI);
			HDDEDATA hRetVal = DdeClientTransaction(NULL, 0ul, hConv, hszItem, CF_TEXT, XTYP_REQUEST, 1000L, &dderesult);

			if (hRetVal != 0) {
				unsigned char *str = DdeAccessData(hRetVal, NULL);
				result = (*env)->NewStringUTF(env, str);

				DdeUnaccessData(hRetVal);
				DdeFreeDataHandle(hRetVal);
			}
			DdeFreeStringHandle(dwidInst, hszItem);
		}
		DdeFreeStringHandle(dwidInst, hszService);
		DdeFreeStringHandle(dwidInst, hszTopic);
		DdeDisconnect(hConv);
	}

    (*env)->ReleaseStringUTFChars(env, app, sapp);
    (*env)->ReleaseStringUTFChars(env, topic, stopic);
    (*env)->ReleaseStringUTFChars(env, item, sitem);
    return result;
}

JNIEXPORT void JNICALL Java_weblx_browser_Win32_ShellExec
  (JNIEnv *env, jobject clazz, jstring jurl)
{
    HWND hDesk = GetDesktopWindow();
    const char *cstr = (*env)->GetStringUTFChars(env, jurl, (jboolean *)NULL);

	ShellExecute(hDesk, "open", cstr, NULL, NULL, 0);
    (*env)->ReleaseStringUTFChars(env, jurl, cstr);
}