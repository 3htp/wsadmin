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
 def SCM_URL = "https://github.com/3htp/webdemo.git";

 /*
def PROJECT="Especificar el nombre del proyecto IIB", sin espacio, ñÑs o caracteres especiales. Es sensible a mayúsculas y minúsculas y no se recomienda que tenga tíldes.
 */
 def PROJECT = "Prueba_Integracion";


 /*
Las siguientes propiedades a especificar corresponde con información exclusiva para el despliegue del(os) .bar generados en Urban Code Deploy.

La información siguiente debe ser proporcionada por el equipo de operaciones, según el ambiente de despliegue asociado.
Importante, son sensibles a mayúsculas y minúsculas.
     
def UCD_COMPONENT="Nombre del componente registrado en urban code deploy";

def UCD_APPLICATION="Nombre de la aplicación registrada en urban code deploy";

def UCD_ENVIRONMENT="Nombre del entorno sobre el cual se realizará el despliegue";

def UCD_PROCESS="Nombre del proceso lanzado por Urban Code Deploy para el despliegue";

def FILE_PATTERN="Patrón o expresión regular que señala los archivos a enviar a Urban Code Deploy para el proceso de despliegue, es importante señalar que para separar varias expresiones se debe utilizar \n ";

def FILE_EXCLUDE_PATTERN="Se especifican los archivos a excluir, también por expresión regular. No se requiere si se han especificado de forma particular los archivos en la variable FILE_PATTERNS";

def UCD_BASEDIR="Especifica la ruta desde la cual se leerán los archivos que serán enviados a Urban Code Deploy para el proceso de despliegue.";
 */
 def UCD_COMPONENT = "WASLRQ";
 def UCD_APPLICATION = "AppWAS_LRQ";
 def UCD_ENVIRONMENT = "Desarrollo";
 def UCD_PROCESS = "InstallWAS";
 def UCD_BASEDIR = "${workspace}";

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
   sh 'cd source'
   sh 'ant'
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
     "pattern": "${workspace}/${PROJECT}.ear",
     "target": "example-repo-local"
    }]
   }
   """

   def buildInfo1 = server.upload spec: uploadSpec
   server.publishBuildInfo buildInfo1

  }

  /*
La fase de deploy lleva los fuentes hasta Urban Code para ser instalados en el servidor que se requiera.

Es indispensable proporcionar los parámetros de UCD_COMPONENT, UCD_APPLICATION, UCD_ENVIRONMENT, UCD_PROCESS, FILE_PATTERN
    
Los UCD_PARAMETERS son proporcionados por el equipo de infraestructura, sin embargo el FilePattern debe ser especificado por el equipo de desarrollo, la misma representa el conjunto de archivos que serán entregados a Urban Code para su respectivo despliegue en el servidor destino.
    
  */

  stage("DEPLOY") {
   echo "[EXEC] - Construyendo script de despliegue";

/*
Notese que se está construyendo el código deploy.sh, mismo desarrollado actualmente por el equipo de integración para entregar los binarios al equipo de infraestructura.

La idea es que o bien se escriba por código los pasos que debe ejecutar Urban Code Deploy para los procesos de despliegue o que se proporcione el archivo deploy.sh a la par de los fuentes.


writeFile file: 'deploy.sh', text: "mqsideploy IIBDESA -e SVR_AFP -a ${PROJECT}.bar -w 1200;"

   echo "[EXEC] - Despliegue sobre Urban Code Deploy ";
   step([
    $class: 'UCDeployPublisher',
    siteName: 'UCD_VOSTPMDE12',
    component: [
     $class: 'com.urbancode.jenkins.plugins.ucdeploy.VersionHelper$VersionBlock',
     componentName: "${UCD_COMPONENT}",
     delivery: [
      $class: 'com.urbancode.jenkins.plugins.ucdeploy.DeliveryHelper$Push',
      pushVersion: "${BUILD_ID}",
      baseDir: "${workspace}",
      fileIncludePatterns: "${FILE_PATTERN}",
      fileExcludePatterns: '',
      pushProperties: '',
      pushDescription: 'Pushed from Jenkins',
      pushIncremental: false
     ]
    ],
    deploy: [
     $class: 'com.urbancode.jenkins.plugins.ucdeploy.DeployHelper$DeployBlock',
     deployApp: "${UCD_APPLICATION}",
     deployEnv: "${UCD_ENVIRONMENT}",
     deployProc: "${UCD_PROCESS}",
     createProcess: [
      $class: 'com.urbancode.jenkins.plugins.ucdeploy.ProcessHelper$CreateProcessBlock',
      processComponent: "${UCD_COMPONENT}"
     ],
     deployVersions: "${UCD_COMPONENT}:${BUILD_ID}",
     deployOnlyChanged: false
    ]
   ])
   */
  }
 }
}
