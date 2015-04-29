#include <jni.h>
#include "../include/shim.h"
#include "../cups-2.0.2/cups/cups.h"
#include <stddef.h>

jobject getJcups_dest_t(JNIEnv *env, cups_dest_t *inval){
	// Prepare array of options;
	jclass option_t = (* env)->FindClass(env, "ml/rabidbeaver/cupsjni/CupsClient/cups_option_t");
	jobjectArray options = (* env)->NewObjectArray(env, inval->num_options, option_t, NULL);
	jmethodID option_inst = (* env)->GetMethodID(env, option_t, "<init>", "(I)V");
	int i;
	for (i=0; i<inval->num_options; i++){
		(* env)->SetObjectArrayElement(env, options, i, (* env)->NewObject(env, option_t, (* env)->NewStringUTF(env,inval->options[i].name), (* env)->NewStringUTF(env,inval->options[i].value)));
	}

	// Prepare destination object;
	jclass dest_t = (* env)->FindClass(env, "ml/rabidbeaver/cupsjni/CupsClient/cups_dest_t");
	jmethodID inst = (* env)->GetMethodID(env, dest_t, "<init>", "(I)V");
	jobject obj = (* env)->NewObject(env, dest_t,
			(* env)->NewStringUTF(env,inval->name),
			(* env)->NewStringUTF(env,inval->instance),
			inval->is_default,
			inval->num_options,
			options);

	return obj;
}

JNIEXPORT jobject JNICALL Java_ml_rabidbeaver_cupsjni_CupsClient_cupsGetDestWithURI(
		JNIEnv *env,
		jobject callingObject,
		jstring name,
		jstring uri){

	// call function from libcups.so:
	cups_dest_t *rval = cupsGetDestWithURI(name,uri);
	if (rval == NULL) return NULL; // don't waste time if we got null, just pass it along.

	jobject obj = getJcups_dest_t(env, rval);

	// Return result;
	return obj;
}

JNIEXPORT jobject JNICALL Java_ml_rabidbeaver_cupsjni_CupsClient_cupsGetDests2(
		JNIEnv *env,
		jobject callingObject,
		jstring url){
	jclass list = (* env)->FindClass(env, "java/util/List");
	jmethodID addMethodID = (* env)->GetMethodID(env, list, "add", "(Ljava/lang/Object;)Z;");
	jmethodID inst = (* env)->GetMethodID(env, list, "<init>", "(I)V");
	jobject obj = (* env)->NewObject(env, list);

	cups_dest_t **rval;
	int results = cupsGetDests2(url, rval);
	int i;
	for (i=0; i<results; i++){
		(* env)->CallBooleanMethod(env, addMethodID, getJcups_dest_t(env, rval[i]));
	}

	return obj;
}
