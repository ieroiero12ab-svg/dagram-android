# Dagram - كيفية بناء الـ APK

## الأحجام المتوقعة بعد البناء

| نوع APK | الحجم | يناسب |
|---------|-------|-------|
| arm64-v8a (Release) | **15-25 MB** | الهواتف الحديثة (2016 وما بعد) - **الموصى به** |
| armeabi-v7a (Release) | **14-22 MB** | الهواتف القديمة |
| Universal (Release) | **25-40 MB** | جميع الهواتف |

> **لماذا هذا الحجم؟** لأننا فعّلنا R8/ProGuard لحذف كل الكود غير المستخدم + ABI splits لبناء APK منفصل لكل معالج.

---

## الخطوة الأولى: تغيير عنوان السيرفر

افتح هذا الملف:
```
app/src/main/java/com/dagram/app/data/api/NetworkModule.kt
```

غيّر هذا السطر:
```kotlin
const val BASE_URL = "https://YOUR_VPS_IP_OR_DOMAIN/api/"
```

**مثال:**
```kotlin
const val BASE_URL = "http://45.90.120.55:3000/api/"
// أو إذا عندك دومين:
const val BASE_URL = "https://dagram.yourdomain.com/api/"
```

كذلك في الملف:
```
app/src/main/java/com/dagram/app/MainActivity.kt
```
غيّر:
```kotlin
wsManager.connect("https://YOUR_VPS_IP_OR_DOMAIN/api/")
```

---

## طريقة 1: GitHub Actions (تلقائي - الأسهل)

1. ارفع الكود على GitHub
2. GitHub يبني APK تلقائياً
3. نزّل من: **Actions → Build Dagram APK → Artifacts**

سيجد APK منفصل لكل معالج - نزّل **arm64-v8a** للهواتف الحديثة.

---

## طريقة 2: Android Studio

1. نزّل **Android Studio** من: `developer.android.com/studio`
2. افتح مجلد `dagram-android`
3. انتظر تحميل المكتبات (5-10 دقائق أول مرة)
4. **Build → Generate Signed APK → Release**
5. ستجد APK في: `app/build/outputs/apk/release/`

---

## نشر الخادم على VPS

```bash
# 1. ثبّت Node.js
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt-get install -y nodejs

# 2. ثبّت pnpm
npm install -g pnpm pm2

# 3. انسخ ملفات المشروع للـ VPS
# ارفع المجلد الرئيسي (artifacts-monorepo) إلى /opt/dagram

# 4. ثبّت المكتبات
cd /opt/dagram
pnpm install

# 5. ضع متغيرات البيئة
export DATABASE_URL="postgresql://user:pass@localhost:5432/dagram"
export JWT_SECRET="ضع-مفتاحاً-سرياً-قوياً-هنا"
export PORT=3000

# 6. شغّل الخادم بشكل دائم
pm2 start "pnpm --filter @workspace/api-server run start" --name dagram
pm2 startup && pm2 save
```

### إعداد Nginx (للـ HTTPS):
```nginx
server {
    listen 80;
    listen 443 ssl;
    server_name yourdomain.com;

    location /api {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
    }

    location /ws {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```
