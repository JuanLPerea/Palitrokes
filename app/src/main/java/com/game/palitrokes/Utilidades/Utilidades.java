package com.game.palitrokes.Utilidades;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public class Utilidades {

    //creamos el fichero donde irá la imagen
    public static Uri crearFicheroImagen() {
        Uri uri_destino = null;
        String nombre_fichero = null;
        File file = null;

        crearNombreArchivo();

        String ruta_captura_foto = crearNombreArchivo();
        Log.d("MIAPP", "RUTA FOTO " + ruta_captura_foto);
        file = new File(ruta_captura_foto);

        try { //INTENTA ESTO

            if (file.createNewFile()) {
                Log.d("MIAPP", "FICHERO CREADO");
            } else {
                Log.d("MIAPP", "FICHERO NO CREADO");
            }
        } catch (IOException e) { // Y SI FALLA SE METE POR AQUÍ
            Log.e("MIAPP", "Error al crear el fichero", e);
        }

        uri_destino = Uri.fromFile(file);
        Log.d("MIAPP", "URI = " + uri_destino.toString());

        return uri_destino;
    }


    // Gracias Vale!!
    public static void desactivarModoEstricto() {
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);

            } catch (Exception e) {
                Log.e(Constantes.TAG, "Error al trucar el método disableDeathOnFileUriExposure", e);
            }
        }
    }


    // Redimensionar Bitmap
    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }


    public static String crearNombreArchivo() {
        // Creamos un nombre de fichero para guardar la foto
        String nombre_fichero = Constantes.PREFIJO_FOTOS + Constantes.SUFIJO_FOTOS;
        String ruta_captura_foto = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/" + nombre_fichero;
        return ruta_captura_foto;
    }

    public static void guardarImagenMemoriaInterna(Context context, String archivo, byte[] byteArray) {
        try {
            FileOutputStream outputStream = context.openFileOutput(archivo + ".jpg", Context.MODE_PRIVATE);
            outputStream.write(byteArray);
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    public static Bitmap recuperarImagenMemoriaInterna(Context context, String archivo) {
        Bitmap bitmap = null;
        System.gc();

        if (archivo == null) {
            archivo = Constantes.ARCHIVO_IMAGEN_JUGADOR;
        }

        try {
            FileInputStream fileInputStream =
                    new FileInputStream(context.getFilesDir().getPath() + "/" + archivo + ".jpg");

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 3;
            options.inTempStorage = new byte[16 * 1024];
            options.inPurgeable = true;

            bitmap = BitmapFactory.decodeStream(fileInputStream, null, options);
        } catch (IOException io) {
            io.printStackTrace();
        }

        return bitmap;
    }

    public static void eliminarArchivo(Context context, String archivo) {

        boolean eliminado = false;

        File file = new File(context.getFilesDir().getPath() + "/" + archivo + ".jpg");
        if (file.exists()) eliminado = file.delete();

    }


    public static byte[] bitmapToArrayBytes(Bitmap bitmap) {

        byte[] arrayBytes = null;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        arrayBytes = stream.toByteArray();

        return arrayBytes;

    }


/*    public static Drawable getAssetImage(Context context, String filename) {

        AssetManager assets = context.getResources().getAssets();
        InputStream buffer = null;
        try {
            buffer = new BufferedInputStream((assets.open("drawable/" + filename + ".png")));
        } catch (IOException e) {
            e.printStackTrace();
        }



        Bitmap bitmap = BitmapFactory.decodeStream(buffer);
        return new BitmapDrawable(context.getResources(), bitmap);


    }*/


    public static boolean eliminarPalabrotas(String palabra) {

        boolean salida = false;

        palabra = palabra.toLowerCase();
        palabra = palabra.replace('á', 'a');
        palabra = palabra.replace('é', 'a');
        palabra = palabra.replace('í', 'a');
        palabra = palabra.replace('ó', 'a');
        palabra = palabra.replace('ú', 'a');


        String arrayPalabrotas[] = {"payaso", "idiota", "subnormal", "tonto", "gilipollas", "puta", "hijoputa", "cabron", "polla", "pene", "fuck", "fuckyou", "chocho", "cojones", "coño", "mierda", "abadol", "abejarruca", "abejarruco", "aberracion", "aberración", "aberrante", "ablanda brevas", "abollao", "aborto", "abrazafarolas", "abrillantaglandes", "abulto", "adán", "afilasables", "aguafiestas", "alacrán", "alcachofo", "alcornoke", "alcornoque", "alela", "alelada", "alelado", "alelao", "alfeñique", "alicate", "alimaña", "almojonao", "almorrana", "alobao", "alsarrabo", "ambercocat", "amochao", "amorfo", "anacoluto", "analfabestia", "animal de acequia", "anormal", "antancanciao", "apollardao", "apollardar", "aragán", "arracacho", "arrastramantas", "arrastrazapatillas", "arreplegat", "arrollapasto", "artosopas", "asalta asilos", "asaltacunas", "asaltapozos", "ase assolellat", "aspamao", "asustatrenes", "atonta", "atontada", "atontado", "atontao", "atroplella platos", "baballo", "babau", "baboso", "bacin", "bacterio", "badanas", "bailabainas", "bailaferias", "bajoca", "bambol", "bandarra", "barjaula", "barriobajero", "bastarda", "bastardo", "bebecharcos", "bebesinsed", "bellaco", "bergante", "berzotas", "besugo", "betzol", "bigardo", "bigotezorra", "biruta", "bleda", "boba", "bobo", "bobolaverga", "bobomierda", "boborremoto", "bocabuzón", "bocachancla", "bocachocho", "bocafiemo", "boiano", "bolascombro", "boludo", "bordegàs", "borinot", "borracha", "borracho", "borrico", "bosses tristes", "bosuso", "botabancals", "boñiga", "brasas", "brincacepas", "brètol", "bufavidres", "bujarra", "bujarron", "bujarrón", "bulebule", "burreras", "burro de set soles", "burxa orelles", "busagre", "buscabregues", "butarut", "butrill", "buzefalo", "cabestro", "cabeza alberca", "cabeza de chorlito", "cabezabuque", "cabezakiko", "cabezalpaca", "cabezantorcha", "cabezarrucho", "cabezon", "cabezón", "cabron", "cabrona", "cabronazo", "cabrón", "cacho mierda", "cachomierda", "cade de fava", "cafre", "caga bandurrias", "cagablando", "cagadubtes", "cagaestacas", "cagalindes", "cagamandurries", "cagamandúrries", "caganio", "cagapoc", "cagaportales", "cagarro", "cagarruta", "cagarrutas", "caguiñas", "caharrán", "calamidad", "calenturas", "calvo", "calzonazos", "camanduleiro", "camastron", "canalla", "candonga", "cansa almas", "cansaliebres", "cansina", "cansino", "cantamañanas", "cap d'ase", "cap de fava", "cap de polla", "cap de ruc", "cap de suro", "capat mental", "capdepesol", "capdetrons", "capillita", "capsigrany", "capsot", "capulla", "capullo", "capuyo", "cara de cona", "carabassa", "caracabello", "caracandao", "caracartón", "carachancla", "caracoño", "caracul", "caraculo", "caraespatula", "caraestaca", "caraestufa", "caraflema", "carahogaza", "carajaula", "carajote", "carallot", "caramierda", "caramirlo", "caranabo", "carantigua", "carapan", "carapapa", "carapena", "caraperro", "carapolla", "caraputa", "carasapo", "carasuela", "caratapena", "cardat del cap", "carnuz", "cascavalero", "castroja", "cateto", "cavallot", "caxomierda", "cazurra", "cazurro", "cebollino", "cenizo", "cenutrio", "ceporro", "cercho", "cercopiteco", "cerillita", "cero a la izquierda", "chafacharcos", "chafalotodo", "chalada", "chalado", "chalao", "chamba na cona", "chanflameja", "chavacana", "chavacano", "chavea", "cheo de moscas", "chichon", "chimpa", "chimpajosé", "chimpamonas", "chimpin", "chingada", "chorlito", "chosco", "chupacables", "chupacharcos", "chupaescrotos", "chupamela", "chupamingas", "chupasangre", "chupatintas", "chupóptero", "chusmón", "cierrabares", "ciervo", "cimborrio", "cipote", "cobachero", "cobarde", "coentrao", "coix de cervell", "colla de feixistes", "collo", "collonera", "comebolsas", "comechapas", "comemierda", "comemierdas", "comemocos", "comepollas", "comeprepucios", "comerreglas", "conacho", "corgao", "cornudo", "correveidile", "cretina", "cretino", "crollo", "cuerdalpaca", "cuerpoescombro", "cui de caronge", "culo panadera", "culoalberca", "culotrapo", "curmaco", "desaborio", "descalzaputas", "descastat", "descerebrado", "descosealpargatas", "desgracia", "desgraciada", "desgraciado", "desgraciao", "despreciable", "destiñe rubias", "desvarata bailes", "desvirgagallines", "don nadie", "donnadie", "down", "durdo", "empaellao", "empana", "empanao", "empujamierda", "en garrovillas", "enderezaplátanos", "energumena", "energumeno", "energúmena", "energúmeno", "engaña baldosas", "engendro", "enjuto", "ennortao", "enrreda bailes", "enze", "escoria", "escuerzo", "escuincle", "esgarramantas", "esjarramantas", "esmirriao", "espantacriatures", "espantajo", "espanyaportes", "esperpento", "estas como un manojo de vergas", "estorbo", "estripa gasones", "estupida", "estupido", "estòtil", "estúpida", "estúpido", "eunuco", "exelol", "facha", "facinerosa", "facineroso", "faino ti lanjran", "falfañote", "fanegas", "fanequero", "fanicerós", "fantoche", "farfoya", "fariseo", "farraguas", "farsante", "fato", "fazañeiro", "fea", "feminazi", "feo", "feodoble", "fervemasber", "feto", "figa tova", "figa-molla", "figaflor", "figamustia", "fill de monja", "fitigote", "follacabras", "follagatos", "follácaros", "forrotimple", "fune", "funesto", "futrul", "gabacho", "gallimarsot", "galocho", "gamarús", "ganapán", "gandul", "gandula", "gandúl", "gansa", "ganso", "garibolo", "garraibat", "garroso", "garrulo", "gaznapiro", "gañan", "gañán", "gili", "gilicoño", "gilipichi", "gilipichis", "gilipipas", "gilipollas", "gilipuerta", "golismero", "gonorrea", "gordinflón", "gordo", "gorrino", "gra de pus", "granuja", "grosera", "grosero", "guarnío", "guarral", "guarrilla", "gumias", "gurmaco", "gusano", "hartosopa", "hartosopas", "hdp", "hijo d puta", "hijo de", "hijo de chacal", "hijo de cura", "hijo de hiena", "hijo de obispo", "hijueputa", "hilh de gaudumela", "horripilante", "huelebragas", "huevon", "huevón", "idiota", "ignorante", "imbecil", "imbécil", "impertinente", "inculto", "inelegante", "infraser", "innortao", "inoperante", "insulso", "inutil", "inútil", "jartible", "jodido", "julai", "lacazan", "ladilla", "lagumán", "lamecharcos", "lamecirios", "lameculos", "lamehuevos", "lameplatos", "lamerajas", "lameñordos", "langran", "langrán", "lapa", "lareta", "larry", "lechuguino", "lechuzo", "lela", "lelo", "lento", "lercho", "lerda", "lerdo", "ligera de cascos", "ligero de cascos", "llepafils", "llimpiatubos", "lumia", "machirulo", "machorra", "machote", "machuno", "maganto", "mahara", "mahareta", "majadero", "majarón", "mal pelat", "malafoya", "malandra", "malandrin", "malasangre", "maleduca", "maleducada", "maleducado", "maleducao", "malfollada", "malnacida", "malnacido", "malparida", "malparido", "mamacallos", "mamahostias", "mamarracho", "mameluco", "mamon", "mamona", "mamporrero", "mamón", "mangarranas", "mangurrian", "mangurrián", "manos gachas", "manos de arbol", "mantecas", "margarito", "maricón", "marimacho", "mascachapas", "mascaxapas", "mastuerzo", "masturbamulos", "masturbaperros", "masturbavacas", "matao", "mataperros", "mazaroco", "me cago en tu madre", "me cago en tu padre", "me cago en tu puta madre", "me cago en tu puto padre", "me cago en tus muertos", "meapilas", "media mierda", "meketrefe", "melindrós", "melona", "melón", "mema", "membrillo", "memo", "mendrugo", "mentecato", "mequetrefe", "merda", "merda d'oca", "merda seca", "merdaseca", "merdellón", "merluzo", "meuca", "microbi", "mierda", "mierdaseca", "milhomes", "mindundi", "mitjamerda", "modorro", "mofeta", "mojigato", "monchi", "mondongo", "moniato", "monigote de feria", "monstrenco", "monstruenco", "monte de esterco", "morlaco", "morral", "morroescopeta", "morroesfinge", "morroestufa", "morrofiemo", "morroperca", "morroputa", "morros de haba", "muerdealmohadas", "muggle", "mugrosa", "mugroso", "napbuf", "necia", "necio", "niñato", "notas", "nugallan", "oligofrenico", "oligofrénico", "ollosdeboto", "osobuco", "otaco", "otako", "otaku", "otakus", "pachón", "pagafantas", "paiaso", "pailán", "pajera", "pajero", "palotrasto", "palurda", "palurdo", "pambisita", "pambisito", "pamplinas", "paneco", "panfigol", "panojas", "panoli", "panto", "panzamelva", "papafrita", "papafritas", "papagayo", "papahostias", "papamoscas", "papanatas", "papirote", "paposo", "papón", "paquete", "pardal", "pardilla", "pardillo", "parguela", "parguelas", "paria", "parvo", "pasiego", "pasmarote", "paspan", "paspon", "paspán", "paspón", "passarell", "pastanaga", "patacrocker", "pataliebre", "patan", "patasdealambre", "patetica", "patetico", "patán", "patética", "patético", "pavisosa", "pavisoso", "pavitonto", "payasa", "payaso", "pazin", "pec des collons", "pecholata", "pechopertiga", "pechugona", "peinaburras", "peinaobejas", "peinaovejas", "peinapochas", "pelabombillas", "pelacantes", "pelacañas", "pelagatos", "pelalimones", "pelamangos", "pelanas", "pelandrusca", "pelarrabos", "pelele", "pellizcacristales", "pelma", "pelmazo", "pelo estropajo", "pelotudo", "pendejo", "percebe", "perepunyetes", "perroflauta", "personajullo", "pescallunes", "petimetre", "picapleitos", "pichabrava", "pichacorta", "pichónaco", "piltrafas", "piltrafillas", "pimpoyo", "pimpín", "pinchacolillas", "pinche", "pinfloid", "pintamonas", "piojoso", "pipa", "pipilla", "pipiolo", "piripi", "pisamostoh", "pisapedales", "pisaverde", "pitañoso", "pitufo", "pixapins", "pixatinters", "pixorro", "plasta", "ploramiques", "plumifero", "poc suc", "pocasluces", "pocasolta", "pollaboba", "pollopera", "pomot", "popona", "portugues", "prea", "pregonao", "pringazorras", "prosma", "psifílico", "pudrecolchones", "pusilanime", "pusilánime", "puta", "putapénico", "putos", "putot", "pánfilo", "pòtol", "pófaro", "rabo", "rata", "remenacacas", "remendafoles", "repelente", "retarded", "retrasado", "retrasubnormal", "retropetuda", "robaperas", "rucio", "ruda", "rudo", "rufían", "ruina", "sabandija", "saborio", "saltacequias", "saltinbanquis", "samarugo", "samugo", "sandalio", "sangonera", "sangre sucia", "sanguijuela", "santurron", "santurrona", "santurrón", "sarnoso", "seboso", "sietemesino", "sinsorgo", "sinsustancia", "somugroso", "somèr", "soplagaitas", "soplanucas", "soplasartenes", "sozagarro", "subnormal", "subnormala", "subnormalo", "subnorpollas", "sunormal", "tacaño", "tap de suro", "tapón de balsa", "tarado", "tarambana", "tarantantan", "taruga", "tarugo", "tarumba", "tastaolletes", "te huele la espalda a baron dandi", "tita freda", "titafluixa", "titafreda", "tocapelotas", "tolai", "tolete", "tonta", "tontaco", "tonto", "tonto dels collons", "tontoculo", "tontoelculo", "tontol'lapiz", "tontolaba", "tontolava", "tontolculo", "tontolhaba", "tontoligo", "tontoloscojones", "cojones", "tontopolla", "tontoprofundo", "tontucio", "torra collons", "torracollons", "totano", "tragabaldas", "tragaldabas", "tragalefas", "tragalpacas", "trampuzas", "trasto", "trepa", "tresnal", "treznal", "troglodita", "trompellot", "tronchobrezo", "tronxamonas", "tros d'ase", "tros de cuoniem", "tros de mula", "tros de puta", "tros de quòniam", "tuercebotas", "turco", "tío plom", "tòtil", "vandalo", "vendehumos", "verga", "vidroid", "vigardo", "votante del pp", "vuelcalitros", "vándalo", "xafandino", "xafarder", "xafaxarcos", "xarrupaescrots", "xingaflautas", "xisgarabis", "xupaxarcos", "yanflameja", "yaro", "yiyon", "zaborrero", "zafio", "zalapastrán", "zamacuco", "zamarraco", "zamarro", "zambergo", "zampabollos", "zanahorio", "zangano", "zangüango", "zangüángano", "zarandajo", "zarrapastrosa", "zarrapastroso", "zarrio", "zascandil", "zopenco", "zoquete", "zornicalo", "zorra", "zote", "zumayo", "zumbado", "zumbao", "zurcemierdas", "zurmaco", "zurremierdas", "zángano",};


        List<String> palabrotas = new ArrayList<>();
        for (int n = 0; n < arrayPalabrotas.length; n++) {
            palabrotas.add(arrayPalabrotas[n]);
        }


        if (palabrotas.contains(palabra)) {
            salida = true;
        }


        return salida;


    }


}
