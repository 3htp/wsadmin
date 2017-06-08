node {



 /*
Especificar el branch desde el cual se descargará el código fuente, con base en 3 variables de entorno diferentes:

${DEV_BRANCH}: Corresponde con el branch developer -> Para desarrollo
${INT_BRANCH}: Corresponde con el branch certification -> Para certificación interna
${REL_BRANCH}: Corresponde con el branch master -> Para producción
 */
 def SCM_BRANCH = "master";

 /*
def SCM_CREDENTIALS="ID_CREDENCIAL";
     
Se debe especificar la credencial creada dentro del vault de credenciales de jenkins para el proyecto.

Es importante destacar que el parametro que debe ser especificado corresponde con el ID y no con información sensible del usuario.
     
 */
 def SCM_CREDENTIALS = "cef486f3-a2d8-496a-81da-283713cf23de";

 /*
def SCM_URL="Especificar la url del proyecto git al cual se armará la arquitectura de integración continua."
 */
 def SCM_URL = "https://github.com/3htp/wsadmin.git";

 /*
def PROJECT="Especificar el nombre del proyecto IIB", sin espacio, ñÑs o caracteres especiales. Es sensible a mayúsculas y minúsculas y no se recomienda que tenga tíldes.
 */
 def PROJECT = "MULTIPIPE_EJEMPLO_v2";

 dir("${workspace}") {

/*La forma de señalar el pipeline es a partir de stages, su definición es en órden secuencial */

/*El pipeline de GET_CODE corresponde con el paso que obtiene el código fuente desde el gitlab oficial de Protección para el proyecto de IIB, el proyecto debe presentar 3 ramas definidas:
        
developer: Corresponde con la rama de desarrollo.
certification: Corresponde con la rama donde se representan los fuentes al momento de entregar el proyecto a evaluación por parte del equipo de certificación.
master: Corresponde con la rama que presenta los fuentes para el ambiente productivo.
    
La idea con la anterior es respetar el esquema de ramas y con base en ello asegurar las fases de entrega para los proyectos.
  */
  stage('Obtener_codigo') {
   sh "rm -rf ${workspace}/*";
   echo "[EXEC] - Obtener codigo fuente desde repositorio Git";
   checkout([
    $class: 'GitSCM',
    branches: [
     [name: "${SCM_BRANCH}"]
    ],
    doGenerateSubmoduleConfigurations: false,
    extensions: [
     [
      $class: 'RelativeTargetDirectory',
      relativeTargetDir: "source"
     ]
    ],
    submoduleCfg: [],
    userRemoteConfigs: [
     [
      credentialsId: "${SCM_CREDENTIALS}",
      url: "${SCM_URL}"
     ]
    ]
   ]);
  }

// -- Compilando
stage('Compilar en ant') {
   echo 'Compilando aplicación'
   
   sh """cd source
   ant"""
  
  } 
    /*
La siguiente fase se encarga del almacenamiento de artefactos o binarios en la herramienta Artifactory, con lo cual se lleva un registro histórico de los artefactos generados.
  */

  stage('Guardar_en_Artifactory') {
   echo "[EXEC] - Almacenando artefactos en Artifactory Server"
   
   def server = Artifactory.server "ArtifactorySaaS"
   
   def uploadSpec = """
   {
    "files": [{
     "pattern": "${workspace}/*.ear",
     "target": "example-repo-local"
    }]
   }
   """

   def buildInfo1 = server.upload spec: uploadSpec
   server.publishBuildInfo buildInfo1

  }

 
  stage("Despliegue WAS") {
   
   

  }
 }
}
