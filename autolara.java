package autolara;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Version 1.0.0
 * @author Lukman N Hakim @lookmaniak, Nov 2019
 */
public class AutoLara {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */

    public static void main(String[] args) throws IOException {  
        
        
        System.out.println("\n\nAutoLara 1.0.0 \nAuthor: Lukman N Hakim @lookmaniak \nNovember, 2019 \n");
        String[] argss;
        BufferedReader rd = new BufferedReader(new InputStreamReader(System.in));   
        String projectPath = System.getProperty("user.dir");
        System.out.println("Current project path:" + projectPath + "\n");
        
        //prepare project and components
        
        System.out.print("Apa nama Class yang akan anda buat? ");
        String componentName = rd.readLine();
        System.out.print("Ketik nama folder untuk module: ");
        String moduleName = rd.readLine();
        if(!"".equals(moduleName)) {
            moduleName = moduleName + "/";
        }
        
        //prepare artisan commands
        String makeModel = "php artisan make:model Modules/" + moduleName + componentName + " -m;";
        String makeResource = "php artisan make:resource " + moduleName + componentName + "Resource;";
        String makeController = "php artisan make:controller " + moduleName + componentName + "Controller --api;";
        
        //execute commands in terminal process
        argss = new String[] {
            "/bin/bash","-c",
            "cd " + projectPath + "; "
                + makeModel
                + makeResource
                + makeController, 
            "with", "args"
        };
        
        Process proc = new ProcessBuilder(argss).start();      
        
        //verifying generated files
        System.out.print("Ketik nama migration file: ");
        String migrationFileName = rd.readLine();
        
        //build file path
        String migrationUri = projectPath + "/database/migrations/"+ migrationFileName + ".php";
        String modelUri = projectPath + "/app/Modules/"+ moduleName + componentName + ".php";
        String resourceUri = projectPath + "/app/Http/Resources/"+ moduleName + componentName +"Resource.php";
        String controllerUri = projectPath + "/app/Http/Controllers/"+ moduleName + componentName + "Controller.php";
        
        //check if required files exist
        boolean migrationFileFound = new File(migrationUri).exists();
        boolean modelFileFound = new File(modelUri).exists();
        boolean resourceFileFound = new File(resourceUri).exists();
        boolean controllerFileFound = new File(controllerUri).exists();
        
        //show file information status
        if(migrationFileFound != true) {
            System.out.println("-- File migration tidak ditemukan!\n" + migrationUri);
        } else {
            System.out.println("-- File migration ok!");
        }
        if(modelFileFound != true) {
            System.out.println("-- File model tidak ditemukan!\n" + modelUri);
        } else {
            System.out.println("-- File model ok!");
        }
        if(resourceFileFound != true) {
            System.out.println("-- File resource tidak ditemukan!\n" + resourceUri);
        } else {
            System.out.println("-- File resource ok!");
        }
        if(controllerFileFound != true) {
            System.out.println("-- File controller tidak ditemukan!\n" + controllerUri);
        } else {
            System.out.println("-- File controller ok!");
        }
        
        //loop if migration file not found
        while(migrationFileFound != true) {
            System.out.println("-- File tidak ditemukan!");
            System.out.print("Ketik nama migration file: ");
            migrationFileName = rd.readLine();
            migrationUri = projectPath + "/database/migrations/"+ migrationFileName;
            if(new File(migrationUri).exists()) {
                migrationFileFound = true;
            }
        }
        
        //prepare file path
        Charset charset = StandardCharsets.UTF_8;
        Path migrationPath = Paths.get(migrationUri);
        Path modelPath = Paths.get(modelUri);
        Path controllerPath = Paths.get(controllerUri);
        
        //Get file content
        String migrationFile = new String(Files.readAllBytes(migrationPath), charset);
        String modelFile = new String(Files.readAllBytes(modelPath), charset);
        String controllerFile = new String(Files.readAllBytes(controllerPath), charset);
        
        //Prepare some line for anchor-tag, so we can modify it letter
        migrationFile = migrationFile.replace("$table->bigIncrements('id');", 
                "//$table-><dataType>('<colName>')<nullAble>;\n\t\t\t//<append>");
        migrationFile = migrationFile.replace("$table->timestamps();", "$table->timestamps();\n\t\t\t//<foreignKey>");
        
        controllerFile = controllerFile.replace("use Illuminate\\Http\\Request;", 
                "use Illuminate\\Http\\Request;\n" + "use App\\Modules\\"+ 
                        moduleName.replace("/","")+"\\"+componentName+";\n" +
                "use App\\Http\\Resources\\"+moduleName.replace("/","")+"\\"+componentName+"Resource;\n"
                );
        
        modelFile = modelFile.replace("//", "protected $fillable = [ \n\t\t\t//<colName> "
                + "\n\t\t\t'created_at', \n\t\t\t'updated_at' \n\t\t]; \n\t\t//<hasMany> \n\t\t//<belongsTo>");

        //Prepare pattern matcher to find double slash in method generated by laravel
        Pattern word = Pattern.compile("//");
        Matcher match = word.matcher(controllerFile);
        StringBuilder stb = new StringBuilder(controllerFile);
        int no=0;
        
        //Find double slash in entire file
        while (match.find()) {
            stb.insert(match.start() + (3 * no), "<"+no+">");
            no++;
        }
        
        //replacing index method on controller
        controllerFile = stb.toString().replace("<0>", "return " + componentName + 
                "Resource::collection(" + componentName + "::all());");
        
        //replacing store method on controller
        controllerFile = controllerFile.replace("<1>", "$" + componentName.toLowerCase() + 
                " = " + componentName + "::create([ \n\t\t\t//<colName>");
        
        //replacing show method on controller
        controllerFile = controllerFile.replace("<2>", "return new " + 
                componentName + "Resource($" + componentName.toLowerCase() + ");");
        
        //replacing update method on controller
        controllerFile = controllerFile.replace("<3>", "$" + componentName.toLowerCase() + 
                "->update($request->only([ \n\t\t\t//<updateColName>");
        
        
        //replacing delete method on controller
        controllerFile = controllerFile.replace("<4>", "$" + componentName.toLowerCase() + 
                "->delete(); \n\t\treturn response()->json(null,204);");
                
        //define column count exclude $table->timestamps(); 
        //note that $table->bigIncrement('id') will be replaced by default
        System.out.print("Tentukan jumlah kolom untuk model: ");
        int colCount = Integer.parseInt(rd.readLine());
        
        //iteration of column definition
        for (int i=0; i < colCount; i++) {
            
            //define column name that will be used in migration, controller, and model
            System.out.print("Masukan nama kolom ke-" + (i+1) + ": ");
            String colName = rd.readLine();
            
            //add column to migration file
            migrationFile = migrationFile.replace("//$table", "$table");
            migrationFile = migrationFile.replace("<colName>", colName);
            
            //change parameter name from [$id]to [ComponentName $componentname]
            controllerFile = controllerFile.replace("$id",componentName + " $" + componentName.toLowerCase());
            
            //add column to create and store method on controller file
            //skip at i = 0, due to ID column can't put to store or update method in controller file
            if(i > 0) {
                controllerFile = controllerFile.replace("//<colName>", "'"+ colName + 
                    "' => $request->" + colName + ", \n\t\t\t//<colName>");
                
                controllerFile = controllerFile.replace("//<updateColName>", "'"+ colName + 
                    "', \n\t\t\t//<updateColName>");
            }
            
            //add column to model File
            modelFile = modelFile.replace("//<colName>", "'" + colName + "',\n\t\t\t//<colName>");
            
            //define laravel datatype for migration
            System.out.print("Masukan jenis tipe data migration untuk kolom [" + 
                    colName + "]: ");
            String dataType = rd.readLine();
            
            //optional length for string datatype
            if(dataType.equals("string")) {
                migrationFile = migrationFile.replace("<dataType>('"+colName+"')", 
                        dataType + "('" + colName +"', <dataLength>)");
                System.out.print("Masukan panjang data kolom " + colName + ": ");
                String dataLength = rd.readLine();
                migrationFile = migrationFile.replace("<dataLength>" , dataLength);
            } else {
                migrationFile = migrationFile.replace("<dataType>", dataType);
            }
            
            //set nullable column if any
            System.out.print("Apakah tipe kolom [" + colName + "] termasuk nullable? Y/N: ");
            String isNullAble = rd.readLine();
            
            if("Y".equalsIgnoreCase(isNullAble) || "Yes".equalsIgnoreCase(isNullAble)) {
                migrationFile = migrationFile.replace("<nullAble>", "->nullable()");
            } else {
                migrationFile = migrationFile.replace("<nullAble>", "");
            }
            
            //add new template on new line for next replacement of iteration
            migrationFile = migrationFile.replace("//<append>", "//$table-><dataType>('<colName>')<nullAble>;\n\t\t\t//<append>");
        }
        
        //add closing mark for store and update method in controller file
        controllerFile = controllerFile.replace("//<colName>//", "//<colName> \n\t\t]);");
        controllerFile = controllerFile.replace("//<updateColName>//", "//<updateColName>\n\t\t]));");
        
        //Add foreign key to model file
        System.out.print("Ada berapa foreign key dalam class [" + componentName + "]? ");
        int foreignCount = Integer.parseInt(rd.readLine());
        
        //iterate foreign key if any
        for (int i = 0; i < foreignCount; i++) {
            //set foreign key column
            System.out.print("Masukan nama kolom foreignKey ke-" + (i+1) + ": ");
            String foreignKey = rd.readLine();
            
            //set referenced table name
            System.out.print("Masukan nama table yang menjadi referensi: ");
            String targetRef = rd.readLine();
            
            //set particular Class name of referenced table
            System.out.print("Masukan nama model Class terkait \n(termasuk folder module apabila Class disimpan dalam folder): ");
            String modelClassTarget = rd.readLine();
            
            //get target foreign class
            String modelTargetUri = projectPath + "/app/Modules/"+ modelClassTarget + ".php";
            
            //check if foreign class is exist
            boolean modelTargetFound = new File(modelTargetUri).exists();
            
            //define skipping parameter
            boolean isSkipped = false;
            
            //loop until foreign class found
            while(modelTargetFound != true) {
                System.out.println("-- File class model target tidak ditemukan!");
                System.out.print("Masukan nama model Class terkait \n(termasuk folder module apabila Class disimpan dalam folder): ");
                modelClassTarget = rd.readLine();
                
                //if user typed 'skip' loop will end, then continue execution
                if("skip".equalsIgnoreCase(modelClassTarget)) {
                    modelTargetFound = true;
                    System.out.println("-- Skipped: Target class model belum memiliki \nmethod hasMany() ke class ini.");
                    isSkipped = true;
                } else {
                    modelTargetUri = projectPath + "/app/Modules/"+ modelClassTarget + ".php";
                    if(new File(modelTargetUri).exists()) {
                        modelTargetFound = true;
                    }
                }
            }
            
            //check if user place foreign Class in subfolder. Supports only 1 subfolder 
            if(modelClassTarget.split("/").length == 2) {
                modelClassTarget = modelClassTarget.split("/")[1];
            }
            
            //create database relational in migration file
            System.out.print("Masukan nama kolom primary_key pada tabel referensi: ");
            String targetPrimary = rd.readLine();
            System.out.print("Masukan parameter onDelete [cascade/set null/no action]: ");
            String onDelete = rd.readLine();
            
            migrationFile = migrationFile.replace("//<foreignKey>","$table->foreign('"+foreignKey+"')\n" +
                "\t\t\t\t->references('"+ targetPrimary +"')->on('"+ targetRef +"')\n" +
                "\t\t\t\t->onDelete('"+ onDelete + "');\n\n\t\t\t//<foreignKey>");
                
            //eachtime  foreign key defined, modify foreign Class and current class with relational method 
            if(!modelFile.contains("belongsTo(" + modelClassTarget)) {
                modelFile = modelFile.replace("//<belongsTo>", "public function "+ modelClassTarget.toLowerCase() +"()\n" +
                                "\t\t{\n" +
                                "\t\t\treturn $this->belongsTo("+ modelClassTarget +"::class);\n" +
                                "\t\t}\n\t\t//<belongsTo>");
            }
            //modify foreign Class for relational table configuration
            if(!isSkipped) {
                Path modelTargetPath = Paths.get(modelTargetUri);
                String modelTargetFile = new String(Files.readAllBytes(modelTargetPath), charset);
                if(!modelTargetFile.contains("hasMany(" + componentName)) {
                    modelTargetFile = modelTargetFile.replace("//<hasMany>","public function "+ componentName.toLowerCase() +"()\n" +
                    "\t\t{\n" +
                    "\t\t\treturn $this->hasMany(" + componentName + "::class);\n" +
                    "\t\t}\n\t\t//<hasMany>");
                }
                //write to foreign target model file
                Files.write(modelTargetPath, modelTargetFile.getBytes(), StandardOpenOption.CREATE);
                System.out.println("-- File class model target berhasil diupdate!");
            }

        }
              
        //write to migration file
        Files.write(migrationPath, migrationFile.getBytes(), StandardOpenOption.CREATE);
        System.out.println("-- File migration berhasil dibuat!");
        //write to controller file
        Files.write(controllerPath, controllerFile.getBytes(), StandardOpenOption.CREATE);
        System.out.println("-- File controller berhasil dibuat!");
        //write to model file
        Files.write(modelPath, modelFile.getBytes(), StandardOpenOption.CREATE);
        System.out.println("-- File model berhasil dibuat!");
        
        //ask user to create route
        System.out.print("Buat route api: ");
        String routeName = rd.readLine();
        
        //update routes/api.php file
        Path p = Paths.get(projectPath + "/routes/api.php");
        String content = "\nRoute::apiResource('" + routeName + "','" + moduleName.replace("/", "\\") + componentName +"Controller');";
        Files.write(p, content.getBytes(), StandardOpenOption.APPEND);
        
        //finish message
        System.out.println("Selesai...! \nAkses end point API di alamat: hostname/api/" + routeName);
        
    }
    
}
