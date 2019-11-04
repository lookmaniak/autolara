# AutoLara


Tools ini dibuat untuk menyederhanakan beberapa proses dalam 
pembuatan sistem menggunakan Framework Laravel. Tools ini hanya
sebatas menangani proses pembuatan backend CRUD dengan konsep api.

Saat anda menjalankan aplikasi ini,
anda akan dipandu dalam pembuatan Class component. Setelah anda
memberikan naama Class dan memberikan nama folder untuk kategori module aplikasi
maka Secara otomatis aplikasi akan mengenerate file.
- Migration 
- Controller
- Resource
- Model

File controller yang tergenerate akan secara otomatis mereferensikan Class model terkait,
dan membuat method-method yang dibutuhkan untuk Resource API.

Ketika anda tidak memberikan nama folder untuk kategori module, maka file-file tersebut
akan berada pada folder defaultnya. Tapi jika anda memberikan nama folder untuk kelompok
module nya maka file tersebut akan berada di:

- Migration [folder default]
- Controller [app/Http/Controllers/<nama module>/ControllerFile.php]
- Resource [app/Http/Resources/<nama module>/ResourceFile.php]
- Model [app/Modules/<nama module>/ModelFile.php]
  
Selanjutnya anda akan diminta untuk memasukan nama file Migration yang telah tergenerate
di folder default migration Laravel.

Setelah file migration diload, anda akan membuat field-field data pada file migration 
melalui aplikasi ini. Kelebihannya adalah, saat anda menambahkan field di file migration,
maka, field yang anda buat akan otomatis dituliskan di file Model pada array $fillable,
dan dituliskan juga di controller pada method Store dan Update.

Jika schema yang anda buat memiliki relasi ke schema lainnya, maka yang perlu anda perhatikan
adalah urutan pembuatan schema. Anda harus memastikan bahwa schema referensi sudah dibuat terlebih
dahulu. Hal ini dikarenakan, aplikasi akan menuliskan secara otomatis method hasMany() dan belongsTo()
di kedua Class Model yang direlasikan, jadi anda tidak perlu berpindah-pindah file untuk menuliskan 
method-method terkait relasi tersebut.

Langkah terakhir adalah membuat route untuk end point api. anda akan diminta memasukan
kata kunci untuk membuat end point tersebut. misal, anda memasukan keyword students
maka api yang digenerate adalah : http://hostname:8000/api/students

Setelah selesai, anda bisa langsung menguji CRUD pada end point api tersebut menggunakan POSTMAN
atau tools sejenis.

Untuk dapat menggunakan tool ini, file autolara.jar disimpan pada
root direktori project Laravel. Kemudian atur CD pada folder project Laravel.
Jalankan file .jar dengan mengeksekusi perintah

java -jar autolara.jar

Sebelumnya, anda harus memiliki Java Runtime pada sistem linux.


