# (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
# http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

LOCAL_PATH:= $(call my-dir)

############################################################################
## cups
############################################################################
# Client library libcups.so
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	cups/adminutil.c \
	cups/array.c \
	cups/attr.c \
	cups/auth.c \
	cups/backchannel.c \
	cups/backend.c \
	cups/conflicts.c \
	cups/custom.c \
	cups/debug.c \
	cups/dest.c \
	cups/dest-job.c \
	cups/dest-localization.c \
	cups/dest-options.c \
	cups/dir.c \
	cups/emit.c \
	cups/encode.c \
	cups/file.c \
	cups/getdevices.c \
	cups/getifaddrs.c \
	cups/getputfile.c \
	cups/globals.c \
	cups/http.c \
	cups/http-addr.c \
	cups/http-addrlist.c \
	cups/http-support.c \
	cups/ipp.c \
	cups/ipp-support.c \
	cups/langprintf.c \
	cups/language.c \
	cups/localize.c \
	cups/mark.c \
	cups/md5.c \
	cups/md5passwd.c \
	cups/notify.c \
	cups/options.c \
	cups/page.c \
	cups/ppd.c \
	cups/ppd-cache.c \
	cups/pwg-media.c \
	cups/request.c \
	cups/sidechannel.c \
	cups/snmp.c \
	cups/snprintf.c \
	cups/string.c \
	cups/tempfile.c \
	cups/thread.c \
	cups/tls.c \
	cups/transcode.c \
	cups/usersys.c \
	cups/util.c \
	filter/error.c \
	filter/raster.c \

disabled_src_files:= \


LOCAL_C_INCLUDES := cups
LOCAL_CFLAGS := -D_PPD_DEPRECATED=
LOCAL_MODULE := libcups
LOCAL_MODULE_TAGS := optional
LOCAL_LDLIBS += -lz


include $(BUILD_SHARED_LIBRARY)

############################################################################
## scheduler
############################################################################
# Server library libcupsmime.so
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	scheduler/filter.c \
	scheduler/mime.c \
	scheduler/type.c \

disabled_src_files:= \


LOCAL_C_INCLUDES := scheduler $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups
LOCAL_CFLAGS := -D_PPD_DEPRECATED=
LOCAL_MODULE := libcupsmime
LOCAL_MODULE_TAGS := optional
LOCAL_LDLIBS += -lz


include $(BUILD_SHARED_LIBRARY)

# Server cupsd
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	scheduler/auth.c \
	scheduler/banners.c \
	scheduler/cert.c \
	scheduler/classes.c \
	scheduler/client.c \
	scheduler/colorman.c \
	scheduler/conf.c \
	scheduler/dirsvc.c \
	scheduler/env.c \
	scheduler/file.c \
	scheduler/main.c \
	scheduler/ipp.c \
	scheduler/listen.c \
	scheduler/job.c \
	scheduler/log.c \
	scheduler/network.c \
	scheduler/policy.c \
	scheduler/printers.c \
	scheduler/process.c \
	scheduler/quotas.c \
	scheduler/select.c \
	scheduler/server.c \
	scheduler/statbuf.c \
	scheduler/subscriptions.c \
	scheduler/sysman.c \

disabled_src_files:= \

LOCAL_C_INCLUDES := scheduler $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups libcupsmime
LOCAL_MODULE := cupsd
LOCAL_MODULE_TAGS := optional
LOCAL_LDLIBS += -lz

include $(BUILD_EXECUTABLE)

# Server cupsfilter
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	scheduler/cupsfilter.c \

disabled_src_files:= \

LOCAL_C_INCLUDES := scheduler $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups libcupsmime
LOCAL_MODULE := cupsfilter
LOCAL_MODULE_TAGS := optional
LOCAL_LDLIBS += -lz

include $(BUILD_EXECUTABLE)

# Server cups-deviced
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	scheduler/cups-deviced.c \
	scheduler/util.c \

disabled_src_files:= \

LOCAL_C_INCLUDES := scheduler $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups libcupsmime
LOCAL_MODULE := cups-deviced
LOCAL_MODULE_TAGS := optional
LOCAL_LDLIBS += -lz

include $(BUILD_EXECUTABLE)

# Server cups-driverd
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	scheduler/cups-driverd.cxx \
	scheduler/util.c \

disabled_src_files:= \

LOCAL_C_INCLUDES := scheduler $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups libcupsppdc
LOCAL_MODULE := cups-driverd
LOCAL_MODULE_TAGS := optional
LOCAL_LDLIBS += -lz

include $(BUILD_EXECUTABLE)

# Server cups-exec
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	scheduler/cups-exec.c \

disabled_src_files:= \

LOCAL_C_INCLUDES := scheduler $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups libcupsmime
LOCAL_MODULE := cups-exec
LOCAL_MODULE_TAGS := optional
LOCAL_LDLIBS += -lz

include $(BUILD_EXECUTABLE)

# Server cups-lpd
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	scheduler/cups-lpd.c \

disabled_src_files:= \

LOCAL_C_INCLUDES := scheduler $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups libcupsmime
LOCAL_MODULE := cups-lpd
LOCAL_MODULE_TAGS := optional
LOCAL_LDLIBS += -lz

include $(BUILD_EXECUTABLE)

############################################################################
## ppdc
############################################################################
# Server libcupsppdc
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	ppdc/ppdc-array.cxx \
	ppdc/ppdc-attr.cxx \
	ppdc/ppdc-catalog.cxx \
	ppdc/ppdc-choice.cxx \
	ppdc/ppdc-constraint.cxx \
	ppdc/ppdc-driver.cxx \
	ppdc/ppdc-file.cxx \
	ppdc/ppdc-filter.cxx \
	ppdc/ppdc-font.cxx \
	ppdc/ppdc-group.cxx \
	ppdc/ppdc-import.cxx \
	ppdc/ppdc-mediasize.cxx \
	ppdc/ppdc-message.cxx \
	ppdc/ppdc-option.cxx \
	ppdc/ppdc-profile.cxx \
	ppdc/ppdc-shared.cxx \
	ppdc/ppdc-source.cxx \
	ppdc/ppdc-string.cxx \
	ppdc/ppdc-variable.cxx \

disabled_src_files:= \

LOCAL_C_INCLUDES := scheduler $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups
LOCAL_MODULE := libcupsppdc
LOCAL_MODULE_TAGS := optional
LOCAL_LDLIBS += -lz

include $(BUILD_SHARED_LIBRARY)

# Server ppdc
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	ppdc/ppdc.cxx \

disabled_src_files:= \

LOCAL_C_INCLUDES := scheduler $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups libcupsppdc
LOCAL_MODULE := ppdc
LOCAL_MODULE_TAGS := optional
LOCAL_LDLIBS += -lz

include $(BUILD_EXECUTABLE)

# Server ppdhtml
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	ppdc/ppdhtml.cxx \

disabled_src_files:= \

LOCAL_C_INCLUDES := scheduler $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups libcupsppdc
LOCAL_MODULE := ppdhtml
LOCAL_MODULE_TAGS := optional
LOCAL_LDLIBS += -lz

include $(BUILD_EXECUTABLE)

# Server ppdi
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	ppdc/ppdi.cxx \

disabled_src_files:= \

LOCAL_C_INCLUDES := scheduler $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups libcupsppdc
LOCAL_MODULE := ppdi
LOCAL_MODULE_TAGS := optional
LOCAL_LDLIBS += -lz

include $(BUILD_EXECUTABLE)

# Server ppdmerge
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	ppdc/ppdmerge.cxx \

disabled_src_files:= \

LOCAL_C_INCLUDES := scheduler $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups libcupsppdc
LOCAL_MODULE := ppdmerge
LOCAL_MODULE_TAGS := optional
LOCAL_LDLIBS += -lz

include $(BUILD_EXECUTABLE)

# Server ppdpo
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	ppdc/ppdpo.cxx \

disabled_src_files:= \

LOCAL_C_INCLUDES := scheduler $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups libcupsppdc
LOCAL_MODULE := ppdpo
LOCAL_MODULE_TAGS := optional
LOCAL_LDLIBS += -lz

include $(BUILD_EXECUTABLE)

############################################################################
## systemv
############################################################################
# admin cancel
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= \
	systemv/cancel.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups
LOCAL_MODULE := cancel
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# admin cupsaccept
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= \
	systemv/cupsaccept.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups
LOCAL_MODULE := cupsaccept
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# admin cupsaddsmb
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= \
	systemv/cupsaddsmb.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups
LOCAL_MODULE := cupsaddsmb
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# admin cupsctl
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= \
	systemv/cupsctl.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups
LOCAL_MODULE := cupsctl
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# admin lp
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= \
	systemv/lp.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups
LOCAL_MODULE := lp
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# admin lpadmin
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= \
	systemv/lpadmin.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups
LOCAL_MODULE := lpadmin
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# admin lpinfo
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= \
	systemv/lpinfo.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups
LOCAL_MODULE := lpinfo
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# admin lpmove
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= \
	systemv/lpmove.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups
LOCAL_MODULE := lpmove
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# admin lpoptions
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= \
	systemv/lpoptions.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups
LOCAL_MODULE := lpoptions
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# admin lpstat
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= \
	systemv/lpstat.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups
LOCAL_MODULE := lpstat
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

############################################################################
## filter
############################################################################
# Server libcupsimage
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	filter/error.c \
	filter/interpret.c \
	filter/raster.c \

disabled_src_files:= \

LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups
LOCAL_MODULE := libcupsimage
LOCAL_MODULE_TAGS := optional

include $(BUILD_SHARED_LIBRARY)

# commandtops
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= filter/commandtops.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups
LOCAL_MODULE := commandtops
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# gziptoany
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= filter/gziptoany.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups
LOCAL_MODULE := gziptoany
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# pstops
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= filter/pstops.c filter/common.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups
LOCAL_MODULE := pstops
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# rastertoepson
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= filter/rastertoepson.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups libcupsimage
LOCAL_MODULE := rastertoepson
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# rastertohp
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= filter/rastertohp.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups libcupsimage
LOCAL_MODULE := rastertohp
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# rastertolabel
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= filter/rastertolabel.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups libcupsimage
LOCAL_MODULE := rastertolabel
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# rastertopwg
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= filter/rastertopwg.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups libcupsimage
LOCAL_MODULE := rastertopwg
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

############################################################################
## cgi-bin
############################################################################
# libcupscgi
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	cgi-bin/help-index.c \
	cgi-bin/html.c \
	cgi-bin/ipp-var.c \
	cgi-bin/search.c \
	cgi-bin/template.c \
	cgi-bin/var.c \	

disabled_src_files:= \

LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups
LOCAL_MODULE := libcupscgi
LOCAL_MODULE_TAGS := optional

include $(BUILD_SHARED_LIBRARY)

# admin.cgi
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= cgi-bin/admin.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups libcupscgi
LOCAL_MODULE := admin.cgi
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# classes.cgi
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= cgi-bin/classes.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups libcupscgi
LOCAL_MODULE := classes.cgi
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# help.cgi
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= cgi-bin/help.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups libcupscgi
LOCAL_MODULE := help.cgi
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# jobs.cgi
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= cgi-bin/jobs.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups libcupscgi
LOCAL_MODULE := jobs.cgi
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# makedocset
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= cgi-bin/makedocset.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups libcupscgi
LOCAL_MODULE := makedocset
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# printers.cgi
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= cgi-bin/printers.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups libcupscgi
LOCAL_MODULE := printers.cgi
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

############################################################################
## berkeley
############################################################################
# lpc
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= berkeley/lpc.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups
LOCAL_MODULE := lpc
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# lpq
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= berkeley/lpq.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups
LOCAL_MODULE := lpq
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# lpr
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= berkeley/lpr.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups
LOCAL_MODULE := lpr
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# lprm
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= berkeley/lprm.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups
LOCAL_MODULE := lprm
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

############################################################################
## backend
############################################################################
# libbackend
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
	backend/ieee1284.c \
	backend/network.c \
	backend/runloop.c \
	backend/snmp-supplies.c \

disabled_src_files:= \

LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups
LOCAL_MODULE := libbackend
LOCAL_MODULE_TAGS := optional

include $(BUILD_STATIC_LIBRARY)

# dnssd (requires dns_sd.h)
#include $(CLEAR_VARS)
#LOCAL_SRC_FILES:= backend/dnssd.c
#LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
#LOCAL_SHARED_LIBRARIES += libcups
#LOCAL_STATIC_LIBRARIES += libbackend
#LOCAL_MODULE := dnssd
#LOCAL_MODULE_TAGS := optional
#include $(BUILD_EXECUTABLE)

# ipp (requires cups/tls.h)
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= backend/ipp.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups
LOCAL_STATIC_LIBRARIES += libbackend
LOCAL_MODULE := ipp
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# lpd
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= backend/lpd.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups
LOCAL_STATIC_LIBRARIES += libbackend
LOCAL_MODULE := lpd
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# snmp
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= backend/snmp.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups
LOCAL_STATIC_LIBRARIES += libbackend
LOCAL_MODULE := snmp
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# socket
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= backend/socket.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups
LOCAL_SHARED_LIBRARIES += libcups
LOCAL_STATIC_LIBRARIES += libbackend
LOCAL_MODULE := socket
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)

# usb
include $(CLEAR_VARS)
LOCAL_SRC_FILES:= backend/usb.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/cups $(LOCAL_PATH)/../libusb
LOCAL_SHARED_LIBRARIES += libcups libusb1.0
LOCAL_STATIC_LIBRARIES += libbackend
LOCAL_MODULE := usb
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)