#include <jni.h>
#include "../include/shim.h"
#include "../cups-2.0.2/cups/cups.h"
#include <stddef.h>

JNIEXPORT jobject JNICALL Java_ml_rabidbeaver_cupsjni_CupsClient_cupsGetDestWithURI(
		JNIEnv *env,
		jobject callingObject,
		jstring name,
		jstring uri){

	// call function from libcups.so:
	cups_dest_t *rval = cupsGetDestWithURI(name,uri);
	if (rval == NULL) return NULL; // don't waste time if we got null, just pass it along.

	// Prepare array of options;
	jclass option_t = (* env)->FindClass(env, "ml/rabidbeaver/cupsjni/CupsClient/cups_option_t");
	jobjectArray options = (* env)->NewObjectArray(env, rval->num_options, option_t, NULL);
	jmethodID option_inst = (* env)->GetMethodID(env, option_t, "<init>", "(I)V");
	int i;
	for (i=0; i<rval->num_options; i++){
		(* env)->SetObjectArrayElement(env, options, i, (* env)->NewObject(env, option_t, (* env)->NewStringUTF(env,rval->options[i].name), (* env)->NewStringUTF(env,rval->options[i].value)));
	}

	// Prepare destination object;
	jclass dest_t = (* env)->FindClass(env, "ml/rabidbeaver/cupsjni/CupsClient/cups_dest_t");
	jmethodID inst = (* env)->GetMethodID(env, dest_t, "<init>", "(I)V");
	jobject obj = (* env)->NewObject(env, dest_t,
			(* env)->NewStringUTF(env,rval->name),
			(* env)->NewStringUTF(env,rval->instance),
			rval->is_default,
			rval->num_options,
			options);

	// Return result;
	return obj;
}
