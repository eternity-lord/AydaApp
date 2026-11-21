# آیدا نیل‌روم | Ayda Nailroom

دفتر روزانه‌ی آفلاین برای مدیریت نوبت‌ها، مشتری‌ها، طراحی‌ها و حساب‌وکتاب یک ناخن‌کار. تمام اطلاعات روی گوشی نگه‌داری می‌شود؛ برنامه حساب کاربری، سرور، اینترنت، تبلیغات یا تحلیل‌گر ندارد.

## دانلود مستقیم

**دانلود APK برای اندروید:** [AydaNailroom-v1.0.0.apk](https://github.com/eternity-lord/AydaApp/releases/download/v1.0.0/AydaNailroom-v1.0.0.apk)

بعد از دانلود، فایل APK را باز کنید و در صورت نمایش پیام اندروید، اجازه‌ی نصب از این منبع را فعال کنید. این نسخه برای نصب مستقیم است و از Google Play دریافت نمی‌شود. حداقل نسخه‌ی پشتیبانی‌شده Android 8.0 (API 26) است.

## امکانات

- رابط کاملاً فارسی و راست‌چین با تقویم شمسی، اعداد فارسی/لاتین و تم تخته‌سیاه و قاب چوبی.
- ثبت مشتری، فرم ناخن، یادداشت و هشدار حساسیت؛ بایگانی مشتری به‌جای پاک‌کردن ناخواسته.
- خدمات قابل ویرایش، نوبت با چند خدمت، محاسبه‌ی مدت و مبلغ، تشخیص تداخل و پیشنهاد زمان خالی.
- یادآوری نوبت با اعلان؛ در صورت ندادن اجازه‌ی آلارم دقیق، زمان‌بندی تقریبی استفاده می‌شود.
- ثبت پرداخت و همگام‌سازی آن با درآمد، ثبت هزینه، مرور ماهانه و فهرست بدهکاران.
- تاریخچه‌ی طرح مشتری و گالری عکس خصوصی با انتخابگر عکس اندروید.
- پشتیبان‌گیری و بازیابی پایگاه‌داده و عکس‌ها با یک فایل ZIP.
- فونت‌های Vazirmatn و Lalezar به‌صورت محلی داخل برنامه.

## ساخت از منبع

نیازمندی‌ها: JDK 17، Android SDK Platform 35 و Build Tools 35.0.0.

```bash
./gradlew testDebugUnitTest assembleDebug
```

فایل خروجی: `app/build/outputs/apk/debug/app-debug.apk`.

هر push و pull request به‌صورت خودکار تست‌های واحد و build را در GitHub Actions اجرا می‌کند. APK آزمایشی به‌عنوان artifact اکشن ذخیره می‌شود؛ نسخه‌ی قابل نصب مستقیم در GitHub Releases قرار می‌گیرد.

## Screenshots

Screenshots will be added after a device/emulator review. The app can be previewed from source by running the debug build on an Android 8.0+ device.

## English

Ayda Nailroom is an offline-first Persian/RTL companion for a nail artist. It keeps client records, Jalali appointments, service pricing, design history, gallery photos, income and expenses on the device. No account, server, analytics, ads, or internet access is required. Android 8.0 (API 26) or newer is required.

Install the APK from the direct download link above. To build locally, use JDK 17 and Android SDK 35, then run `./gradlew testDebugUnitTest assembleDebug`.
