/*
* Shell `simplesh` (basado en el shell de xv6)
*
* Ampliación de Sistemas Operativos
* Departamento de Ingeniería y Tecnología de Computadores
* Facultad de Informática de la Universidad de Murcia
*
* Alumnos: APELLIDOS, NOMBRE (GX.X)
*          APELLIDOS, NOMBRE (GX.X)
*
* Convocatoria: FEBRERO/JUNIO/JULIO
*/


/*
 * Ficheros de cabecera
 */


#define _POSIX_C_SOURCE 200809L /* IEEE 1003.1-2008 (véase /usr/include/features.h) */
//#define NDEBUG                /* Traduce asertos y DMACROS a 'no ops' */

#include <assert.h>
#include <errno.h>
#include <fcntl.h>
#include <getopt.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <signal.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>
#include <pwd.h>
#include <limits.h>
#include <libgen.h>
// Biblioteca readline
#include <readline/readline.h>
#include <readline/history.h>
#include <stdbool.h>
#include <sys/types.h>
#include <dirent.h>


/******************************************************************************
 * Constantes, macros y variables globales
 ******************************************************************************/

#define MAX_TAM_BYTES 1048576
#define DEFAULT_BSIZE 1024
#define MIN_DEF_BSIZE 10
#define SPLIT_LINES 1
#define SPLIT_BYTES 2
#define MAX_PROCS 8

static const char* VERSION = "0.18";

// Niveles de depuración
#define DBG_CMD   (1 << 0)
#define DBG_TRACE (1 << 1)
// . . .
static int g_dbg_level = 0;

#ifndef NDEBUG
#define DPRINTF(dbg_level, fmt, ...)                            \
    do {                                                        \
        if (dbg_level & g_dbg_level)                            \
            fprintf(stderr, "%s:%d:%s(): " fmt,                 \
                    __FILE__, __LINE__, __func__, ##__VA_ARGS__);       \
    } while ( 0 )

#define DBLOCK(dbg_level, block)                                \
    do {                                                        \
        if (dbg_level & g_dbg_level)                            \
            block;                                              \
    } while( 0 );
#else
#define DPRINTF(dbg_level, fmt, ...)
#define DBLOCK(dbg_level, block)
#endif

#define TRY(x)                                                  \
    do {                                                        \
        int __rc = (x);                                         \
        if( __rc < 0 ) {                                        \
            fprintf(stderr, "%s:%d:%s: TRY(%s) failed\n",       \
                    __FILE__, __LINE__, __func__, #x);          \
            fprintf(stderr, "ERROR: rc=%d errno=%d (%s)\n",     \
                    __rc, errno, strerror(errno));              \
            exit(EXIT_FAILURE);                                 \
        }                                                       \
    } while( 0 )


// Número máximo de argumentos de un comando
#define MAX_ARGS 16
// Delimitadores
static const char WHITESPACE[] = " \t\r\n\v";
// Caracteres especiales
static const char SYMBOLS[] = "<|>&;()";
static sigset_t signal_child;
int pids_procesos[MAX_PROCS];
pid_t * array_procesos;

/******************************************************************************
 * Funciones auxiliares
 ******************************************************************************/
int olddir = 0;
int es_exit = 0;

int nuevo_comando = 0;

// Imprime el mensaje
void info(const char *fmt, ...)
{
    va_list arg;

    fprintf(stdout, "%s: ", __FILE__);
    va_start(arg, fmt);
    vfprintf(stdout, fmt, arg);
    va_end(arg);
}


// Imprime el mensaje de error
void error(const char *fmt, ...)
{
    va_list arg;

    fprintf(stderr, "%s: ", __FILE__);
    va_start(arg, fmt);
    vfprintf(stderr, fmt, arg);
    va_end(arg);
}





// Imprime el mensaje de error y aborta la ejecución
void panic(const char *fmt, ...)
{
    va_list arg;

    fprintf(stderr, "%s: ", __FILE__);
    va_start(arg, fmt);
    vfprintf(stderr, fmt, arg);
    va_end(arg);

    exit(EXIT_FAILURE);
}


// `fork()` que muestra un mensaje de error si no se puede crear el hijo
int fork_or_panic(const char* s) {
    int pid;

    pid = fork();
    if (pid == -1)
        panic("%s failed: errno %d (%s)", s, errno, strerror(errno));
    return pid;
}

int open_file(char * nombre_fichero) {
    int fd = -1;
    if (nuevo_comando == 0) {
        fd = open(nombre_fichero, O_RDWR | O_CREAT | O_TRUNC, S_IRWXU);
        nuevo_comando++;
    } else {
        fd = open(nombre_fichero, O_RDWR | O_CREAT | O_APPEND, S_IRWXU);
    }
    return fd;
}

ssize_t write_bytes(int fd, char * buffer, int inicio, ssize_t bytes) {
    ssize_t bytes_escritos = 0;
    int bytes_total_escritos = 0;
    ssize_t pos_origen = inicio;
    ssize_t tam_bytes = bytes;
    while (tam_bytes > 0) {
        bytes_escritos = write(fd, buffer + pos_origen, (size_t) tam_bytes);
        bytes_total_escritos += bytes_escritos;
        pos_origen += bytes_total_escritos;
        tam_bytes -= bytes_escritos;
    }
    return bytes_total_escritos;
}

ssize_t read_small_chunks(int fd, char* buffer, int tam, int tam_max) {
    ssize_t total_leidos = 0;
    ssize_t leidos = 0;
    do {
        leidos = read(fd, buffer + total_leidos, tam);
        total_leidos += leidos;
    } while (total_leidos != tam_max && leidos > 0);
    return total_leidos;
}

void run_psplit_lines_from_file(int fd, char * fichero, int num_max_lineas, size_t tam_bytes, int procesos) {
    if (fd == STDIN_FILENO) {
        fichero = "stdin";
    }
    int fd_file = -1;
    ssize_t bytes_leidos = 0;
    char  * buffer ;
    int contador_ficheros = 0;
    char nombre_fichero[60];
    sprintf(nombre_fichero, "%s%d", fichero, contador_ficheros);

    fd_file = open_file(nombre_fichero);
    ssize_t pos_origen = 0;
    ssize_t pos_actual = 0;
    int contador_lineas = 0;

    if (tam_bytes < MIN_DEF_BSIZE) {
        buffer = malloc ((tam_bytes * 100) * sizeof(char));
        bytes_leidos = read_small_chunks(fd, buffer, tam_bytes, tam_bytes * 100);
    } else {
        buffer = malloc (tam_bytes * sizeof(char));
        bytes_leidos = read(fd, buffer, tam_bytes);
    }

    while (bytes_leidos != 0 ) {
        ssize_t contador = 0;
        pos_origen = 0;
        while(buffer[contador] != '\0' && contador < bytes_leidos) {
            if (buffer[contador] == '\n') {
                contador_lineas++;
                pos_actual = contador + 1;
                ssize_t bytes_restantes = (pos_actual - pos_origen);
                write_bytes(fd_file, buffer, (int) pos_origen, bytes_restantes);
                pos_origen = pos_actual;
                if (contador_lineas == num_max_lineas) {
                    contador_lineas = 0;
                    contador_ficheros++;
                    fsync(fd_file);
                    close(fd_file);
                    sprintf(nombre_fichero, "%s%d", fichero, contador_ficheros);
                    nuevo_comando = 0;
                    fd_file = open_file(nombre_fichero);
                }
            }
            contador++;
        }
        memset(buffer, '\0', tam_bytes);

        if (tam_bytes < MIN_DEF_BSIZE) {
            bytes_leidos = read_small_chunks(fd, buffer, tam_bytes, tam_bytes * 100);
        } else {
            bytes_leidos = read(fd, buffer, tam_bytes);
        }

    }
    if (pos_origen < bytes_leidos) {
        ssize_t bytes_escritos = 0;
        ssize_t bytes_restantes = (bytes_leidos - pos_origen);
        while(tam_bytes > 0) {
            bytes_escritos = write(fd_file, buffer + pos_origen, bytes_restantes);
            bytes_restantes -= bytes_escritos;
        }

    }
    free(buffer);
}


void split_bytes_bt_size(int fd, char *fichero, ssize_t tam_chunk_bytes, ssize_t buffer_size) {
    ssize_t  bytes_leidos = 0;
    char buffer[buffer_size];
    int num_ficheros = 0;
    int fd_salida = -1;
    ssize_t bytes_restantes = tam_chunk_bytes;
    nuevo_comando = 0;
    memset(buffer, '\0', buffer_size);
    char nombre_fichero[60];
    sprintf(nombre_fichero, "%s%d", fichero, num_ficheros);

    bytes_leidos = read(fd, buffer, (size_t) buffer_size);
    ssize_t bytes_escritos = 0;
    while(bytes_leidos > 0) {
        fd_salida = open_file(nombre_fichero);
        while (bytes_restantes > bytes_leidos && bytes_leidos > 0) {
            bytes_escritos = write_bytes(fd_salida, buffer, 0, bytes_leidos);
            bytes_restantes -= bytes_escritos;
            bytes_leidos = read(fd, buffer, (size_t) buffer_size);
        }
        if (bytes_restantes > 0 && bytes_leidos > 0) {
            bytes_escritos = write_bytes(fd_salida, buffer, 0, bytes_restantes);
            bytes_leidos -= bytes_escritos;

        }
        fsync(fd_salida);
        close(fd_salida);
        bytes_restantes = tam_chunk_bytes;
        num_ficheros++;
        nuevo_comando = 0;
        sprintf(nombre_fichero, "%s%d", fichero, num_ficheros);
        if (bytes_leidos > 0) {
            fd_salida = open_file(nombre_fichero);
            bytes_escritos = write_bytes(fd_salida, buffer, (int) bytes_escritos, bytes_leidos);
            bytes_restantes -= bytes_escritos;
        }
        bytes_leidos = read(fd, buffer, (size_t) buffer_size);
    }
    fsync(fd_salida);
    close(fd_salida);
}


void run_split_bytes_from_file(int fd, char *  fichero, ssize_t tam_chunk_bytes, ssize_t buffer_size) {
    if (fd == STDIN_FILENO) {
        fichero = "stdin";
    }
    char buffer[buffer_size];
    ssize_t bytes_total_leidos = 0;
    ssize_t bytes_leidos = 0;
    int contador_fichero = 0;
    int pos_origen = 0;
    nuevo_comando = 0;

    char fd_fichero_salida[60];
    sprintf(fd_fichero_salida, "%s%d", fichero , contador_fichero);
    int fd_salida = open_file(fd_fichero_salida);
    int tam_total_escrito = 0;
    while((bytes_leidos = read(fd, buffer + bytes_total_leidos, (size_t) buffer_size)) > 0){         // leo
        bytes_total_leidos += bytes_leidos;                                                                                 // lo que he leido, lo añado a lo que he leido en total
        if(tam_total_escrito == tam_chunk_bytes && bytes_total_leidos > 0){                                                 // si lo que he escrito es igual al tamaño de chunk del flag -b y aun me quedan bytes leidos
            fsync(fd_salida);                                                                                               // cierro fichero y abro uno nuevo
            close(fd_salida);
            contador_fichero++;
            nuevo_comando = 0;
            sprintf(fd_fichero_salida, "%s%d" , fichero ,contador_fichero);
            fd_salida = open_file(fd_fichero_salida);
            tam_total_escrito = 0;
        }
        while(tam_total_escrito < tam_chunk_bytes && bytes_total_leidos > 0){                                           // mientras no hemos esccrito hasta el tam del chunk y nos queden bytes leidos
            ssize_t bytes_restantes = (tam_chunk_bytes - tam_total_escrito);
            if (bytes_total_leidos <= bytes_restantes) {                                                                // Si lo que he leido es menor que lo que tengo que escribir
                ssize_t escritos = write_bytes(fd_salida,buffer, pos_origen, (size_t) bytes_total_leidos);              // escribo aquello que he leido
                bytes_total_leidos -= escritos;                                                                         // decremento los bytes que llevo leidos
                tam_total_escrito = (int) (tam_total_escrito + escritos);                                               // actualizo el total de bytes escritos
                pos_origen = 0;                                                                                         // reinicio el mi posicion de ofset
            } else {                                                                                                    // En caso de lo que he leido es mas de lo que me queda por escribir
                ssize_t escritos = write_bytes(fd_salida,buffer, pos_origen , (size_t) bytes_restantes);                // Escribo lo que me queda por escribir
                bytes_total_leidos -= bytes_restantes;                                                                  // actualizo los que me queda leido
                tam_total_escrito += escritos;                                                                          // actualizo lo que he escrito en total
                pos_origen += bytes_restantes;                                                                          // actualizo mi offset para saber en que posicion del buffer tengo que escribir
                if(tam_total_escrito == tam_chunk_bytes) {                                                              // si lo que he escrito en total es igual al tam de chunk definido por el flag -b:
                    fsync(fd_salida);                                                                                   //  Sincronizo, cierro fichero y creo uno nuevo
                    close(fd_salida);
                    contador_fichero++;
                    nuevo_comando = 0;
                    sprintf(fd_fichero_salida,"%s%d",fichero,contador_fichero);
                    fd_salida = open_file(fd_fichero_salida);
                    tam_total_escrito = 0;
                }
            }
        }
    }
    close(fd_salida);
}


void process_psplit_command(int fd, char * fichero, int flag, int cantidad, int tam_bytes) {
    switch (flag) {
        case SPLIT_LINES:
            run_psplit_lines_from_file(fd, fichero, cantidad, (size_t) tam_bytes, 0);
            break;

        case SPLIT_BYTES:
            run_split_bytes_from_file(fd, fichero, cantidad, tam_bytes);
            break;
        default:
            break;
    }

}

void run_psplit(int argc, char **argv) {
    optind = 1;
    int opt, arg, flag;
    int tam_bytes = 0;
    int hay_fichero = 0;
    int flags_incompatibles = 0;
    int procesos_totales = 1;
    while ((opt = getopt(argc, argv, "l:b:s:p:h")) != -1) {
        switch (opt) {
            case 'l':
                if (flags_incompatibles > 0) {
                    printf("psplit: Opciones incompatibles\n");
                    return;
                } else {
                    arg = atoi(optarg);
                    flag = SPLIT_LINES;
                    flags_incompatibles++;
                }
                break;
            case 'b':
                if (flags_incompatibles > 0) {
                    printf("psplit: Opciones incompatibles\n");
                    return;
                } else {
                    arg = atoi(optarg);
                    flag = SPLIT_BYTES;
                    flags_incompatibles++;
                }
                break;

            case 's':
                tam_bytes = atoi(optarg);
                if (tam_bytes < 1 || tam_bytes > MAX_TAM_BYTES) {
                    printf("psplit: Opción -s no válida\n");
                    return;
                }
                break;
            case 'p':
                procesos_totales = atoi(optarg);
                if (procesos_totales <= 0) {
                    printf("psplit: Opción -p no válida\n");
                    return;
                }
                break;
            case 'h':
                printf("Uso: psplit [-l NLINES] [-b NBYTES] [-s BSIZE] [-p PROCS] [FILE1] [FILE2]...\n");
                printf("\t Opciones:\n");
                printf("\t -l NLINES Número máximo de líneas por fichero.\n");
                printf("\t -b NBYTES Número máximo de bytes por fichero.\n");
                printf("\t -s BSIZE  Tamaño en bytes de los bloques leídos de [FILEn] o stdin.\n");
                printf("\t -p PROCS  Número máximo de procesos_totales simultáneos.\n");
                printf("\t -h        Ayuda\n");
                printf("\n");

                return;
            default:
                break;
        }
    }

    if (tam_bytes == 0)
        tam_bytes = DEFAULT_BSIZE;

    int num_procesos = 0;
    hay_fichero = argc - optind;
    if (hay_fichero > 0) {
        if (procesos_totales > 1) {
            array_procesos = malloc(procesos_totales * sizeof(int));
            memset(array_procesos, 0, procesos_totales);
            for (int i = optind; i < argc; ++i) {
                int fd = open(argv[i], O_RDONLY);
                pid_t pid;
                if (num_procesos < procesos_totales) {
                    //printf("Hay sitio, aun quedan %d libres\n", procesos_totales - num_procesos);
                    num_procesos++;
                    if ((pid = fork_or_panic("")) == 0) {
                        process_psplit_command(fd, argv[i], flag, arg, tam_bytes);
                        close(fd);
                        _exit(0);
                    }
                    for (int j = 0; j < procesos_totales; ++j) {
                        if (array_procesos[i] == 0) {
                            array_procesos[i] = pid;
                            break;
                        }
                    }
                    wait(NULL);
                } else {
                    pid_t primer_pid_libre = 0;
                    for (int j = 0; j < procesos_totales; ++j) {
                        if (array_procesos[i] > 0) {
                            primer_pid_libre = array_procesos[i];
                            array_procesos[i] = 0;
                            break;
                        }
                    }
                    waitpid(primer_pid_libre, NULL, 0);
                    num_procesos--;
                }
            }
            free(array_procesos);
        } else {
            for (int i = optind; i < argc; ++i) {
                int fd = open(argv[i], O_RDONLY);
                process_psplit_command(fd, argv[i], flag, arg, tam_bytes);
                close(fd);
            }
        }
    } else {
        process_psplit_command(STDIN_FILENO, NULL, flag, arg, tam_bytes);
    }

    /*if (hay_fichero > 0) {
        for (int i = optind; i < argc; ++i) {
            int fd = open(argv[i], O_RDONLY);
            process_psplit_command(fd, argv[i], flag, arg, tam_bytes);
            close(fd);
        }
    } else {
        process_psplit_command(STDIN_FILENO, NULL, flag, arg, tam_bytes);
    }*/

}



void run_cwd() {
    char ruta[PATH_MAX];
    if (!getcwd(ruta, PATH_MAX)) {
        perror("FALLO DE RUTA");
        exit(EXIT_FAILURE);
    }
    printf("cwd: %s\n", ruta);
}

char * get_cur_dir() {
    char * ruta = malloc(PATH_MAX * sizeof(char*));
    if (!getcwd(ruta, PATH_MAX)) {
        perror("FALLO DE RUTA");
        exit(EXIT_FAILURE);
    }
    return ruta;
}

void run_cd_HOME() {
    char * dir_actual = get_cur_dir();
    char * dir_home = getenv("HOME");
    chdir(dir_home);
    setenv("OLDPWD", dir_actual, true);
}

void run_cd(char* path) {
    if (strcmp(path, "-") == 0) {
        char * dir = getenv("OLDPWD");
        if (dir != NULL && olddir > 0) {
            run_cd(dir);
        } else {
            printf("run_cd: Variable OLDPWD no definida\n");
            // exit(EXIT_FAILURE);
        }
    } else {
        DIR* dir = opendir(path);
        if(!dir) {
            printf("run_cd: No existe el directorio '%s'\n", path);
        } else {
            char * dir_actual = get_cur_dir();
            if (strcmp(dir_actual, "") == 0) {
                perror("run_cd: No existe el directorio\n");
            } else {
                int succes = chdir(path);
                if (succes == 0) {
                    setenv("OLDPWD", dir_actual, true);
                    olddir = 1;
                } else {
                    perror("NO se ha podido cambiar de directorio\n");
                }
            }
        }
    }

}

/******************************************************************************
 * Estructuras de datos `cmd`
 ******************************************************************************/


// Las estructuras `cmd` se utilizan para almacenar información que servirá a
// simplesh para ejecutar líneas de órdenes con redirecciones, tuberías, listas
// de comandos y tareas en segundo plano. El formato es el siguiente:

//     |----------+--------------+--------------|
//     | (1 byte) | ...          | ...          |
//     |----------+--------------+--------------|
//     | type     | otros campos | otros campos |
//     |----------+--------------+--------------|

// Nótese cómo las estructuras `cmd` comparten el primer campo `type` para
// identificar su tipo. A partir de él se obtiene un tipo derivado a través de
// *casting* forzado de tipo. Se consigue así polimorfismo básico en C.

// Valores del campo `type` de las estructuras de datos `cmd`
enum cmd_type { EXEC=1, REDR=2, PIPE=3, LIST=4, BACK=5, SUBS=6, INV=7 };

struct cmd { enum cmd_type type; };

// Comando con sus parámetros
struct execcmd {
    enum cmd_type type;
    char* argv[MAX_ARGS];
    char* eargv[MAX_ARGS];
    int argc;
};

// Comando con redirección
struct redrcmd {
    enum cmd_type type;
    struct cmd* cmd;
    char* file;
    char* efile;
    int flags;
    mode_t mode;
    int fd;
};

// Comandos con tubería
struct pipecmd {
    enum cmd_type type;
    struct cmd* left;
    struct cmd* right;
};

// Lista de órdenes
struct listcmd {
    enum cmd_type type;
    struct cmd* left;
    struct cmd* right;
};

// Tarea en segundo plano (background) con `&`
struct backcmd {
    enum cmd_type type;
    struct cmd* cmd;
};

// Subshell
struct subscmd {
    enum cmd_type type;
    struct cmd* cmd;
};


/******************************************************************************
 * Funciones para construir las estructuras de datos `cmd`
 ******************************************************************************/


// Construye una estructura `cmd` de tipo `EXEC`
struct cmd* execcmd(void)
{
    struct execcmd* cmd;

    if ((cmd = malloc(sizeof(*cmd))) == NULL)
    {
        perror("execcmd: malloc");
        exit(EXIT_FAILURE);
    }
    memset(cmd, 0, sizeof(*cmd));
    cmd->type = EXEC;

    return (struct cmd*) cmd;
}

// Construye una estructura `cmd` de tipo `REDR`
struct cmd* redrcmd(struct cmd* subcmd,
                    char* file, char* efile,
                    int flags, mode_t mode, int fd)
{
    struct redrcmd* cmd;

    if ((cmd = malloc(sizeof(*cmd))) == NULL)
    {
        perror("redrcmd: malloc");
        exit(EXIT_FAILURE);
    }
    memset(cmd, 0, sizeof(*cmd));
    cmd->type = REDR;
    cmd->cmd = subcmd;
    cmd->file = file;
    cmd->efile = efile;
    cmd->flags = flags;
    cmd->mode = mode;
    cmd->fd = fd;

    return (struct cmd*) cmd;
}

// Construye una estructura `cmd` de tipo `PIPE`
struct cmd* pipecmd(struct cmd* left, struct cmd* right)
{
    struct pipecmd* cmd;

    if ((cmd = malloc(sizeof(*cmd))) == NULL)
    {
        perror("pipecmd: malloc");
        exit(EXIT_FAILURE);
    }
    memset(cmd, 0, sizeof(*cmd));
    cmd->type = PIPE;
    cmd->left = left;
    cmd->right = right;

    return (struct cmd*) cmd;
}

// Construye una estructura `cmd` de tipo `LIST`
struct cmd* listcmd(struct cmd* left, struct cmd* right)
{
    struct listcmd* cmd;

    if ((cmd = malloc(sizeof(*cmd))) == NULL)
    {
        perror("listcmd: malloc");
        exit(EXIT_FAILURE);
    }
    memset(cmd, 0, sizeof(*cmd));
    cmd->type = LIST;
    cmd->left = left;
    cmd->right = right;

    return (struct cmd*)cmd;
}

// Construye una estructura `cmd` de tipo `BACK`
struct cmd* backcmd(struct cmd* subcmd)
{
    struct backcmd* cmd;

    if ((cmd = malloc(sizeof(*cmd))) == NULL)
    {
        perror("backcmd: malloc");
        exit(EXIT_FAILURE);
    }
    memset(cmd, 0, sizeof(*cmd));
    cmd->type = BACK;
    cmd->cmd = subcmd;

    return (struct cmd*)cmd;
}

// Construye una estructura `cmd` de tipo `SUB`
struct cmd* subscmd(struct cmd* subcmd)
{
    struct subscmd* cmd;

    if ((cmd = malloc(sizeof(*cmd))) == NULL)
    {
        perror("subscmd: malloc");
        exit(EXIT_FAILURE);
    }
    memset(cmd, 0, sizeof(*cmd));
    cmd->type = SUBS;
    cmd->cmd = subcmd;

    return (struct cmd*) cmd;
}

void run_exit(struct execcmd * ecmd) {
    es_exit = 1;
}

/******************************************************************************
 * Funciones para realizar el análisis sintáctico de la línea de órdenes
 ******************************************************************************/


// `get_token` recibe un puntero al principio de una cadena (`start_of_str`),
// otro puntero al final de esa cadena (`end_of_str`) y, opcionalmente, dos
// punteros para guardar el principio y el final del token, respectivamente.
//
// `get_token` devuelve un *token* de la cadena de entrada.

int get_token(char** start_of_str, char* end_of_str,
              char** start_of_token, char** end_of_token)
{
    char* s;
    int ret;

    // Salta los espacios en blanco
    s = *start_of_str;
    while (s < end_of_str && strchr(WHITESPACE, *s))
        s++;

    // `start_of_token` apunta al principio del argumento (si no es NULL)
    if (start_of_token)
        *start_of_token = s;

    ret = *s;
    switch (*s)
    {
        case 0:
            break;
        case '|':
        case '(':
        case ')':
        case ';':
        case '&':
        case '<':
            s++;
            break;
        case '>':
            s++;
            if (*s == '>')
            {
                ret = '+';
                s++;
            }
            break;

        default:

            // El caso por defecto (cuando no hay caracteres especiales) es el
            // de un argumento de un comando. `get_token` devuelve el tam_chunk_bytes
            // `'a'`, `start_of_token` apunta al argumento (si no es `NULL`),
            // `end_of_token` apunta al final del argumento (si no es `NULL`) y
            // `start_of_str` avanza hasta que salta todos los espacios
            // *después* del argumento. Por ejemplo:
            //
            //     |-----------+---+---+---+---+---+---+---+---+---+-----------|
            //     | (espacio) | a | r | g | u | m | e | n | t | o | (espacio)
            //     |
            //     |-----------+---+---+---+---+---+---+---+---+---+-----------|
            //                   ^                                   ^
            //            start_o|f_token                       end_o|f_token

            ret = 'a';
            while (s < end_of_str &&
                   !strchr(WHITESPACE, *s) &&
                   !strchr(SYMBOLS, *s))
                s++;
            break;
    }

    // `end_of_token` apunta al final del argumento (si no es `NULL`)
    if (end_of_token)
        *end_of_token = s;

    // Salta los espacios en blanco
    while (s < end_of_str && strchr(WHITESPACE, *s))
        s++;

    // Actualiza `start_of_str`
    *start_of_str = s;

    return ret;
}



//************************************************************************
//*          FUNCIONES DE LA PRÁCTICA
//************************************************************************


//Funcion auxiliar para darle la vuelva al a una cadena.
/**
 * Esta
 * @param begin
 * @param end
 */
void cadenaInversa(char *begin, char *end)
{
    char aux;
    while(end>begin)
        aux=*end, *end--=*begin, *begin++=aux;
}

/**
 * Esta función se utiliza para convertir un entero a string
 * @param value El Entero que se quiere transformar
 * @param str El buffer en el que se inserta el entero
 */
void integerToString(int value, char *str)
{
    char* wstr=str;
    int sign;
    div_t res;

    if ((sign=value) < 0) value = -value;

    do {
        *wstr++ = (char) ((value % 10) + '0');
    }while((value=value/10) > 0);

    if(sign<0) *wstr++='-';
    *wstr='\0';

    cadenaInversa(str, wstr - 1);
}


/**
 * Comprueba que un proceso está en segundo plano
 * @param pid  El proceso del que se quiere hacer la comprobación
 * @return  Devuelve el valor 0 si es el proceso esta en segundo plano. Devuelve 1 si no lo esta
 */
int is_back_process(int pid){
    if (pid < 0) return 1;
    int i = 0 ;
    while(i < MAX_PROCS){
        if(pid == pids_procesos[i]) return 0;
        i++;
    }
    return 1;
}


/**
 * Esta funcion sirve para calcular el numero de cifras de un valor Integer.
 * @param numero El numero de tipo Integer
 * @return Devuelve el numero de cifras que tiene x
 */
int longitud_numero(int numero) {
    int cifras = 0;
    while (numero > 0) {
        numero /= 10;
        cifras++;
    }
    return cifras < 1 ? 1 : cifras;
}


//Funcion auxiliar para imprimir el PID que nos pasan como parametro.
/**
 * Imprime un proceso en el formato [ PID ]
 * @param pid Proceso que se quiere imprimir
 */
void print_processid(int pid){
    int longitud = longitud_numero(pid);

    char barra1= '[';
    write(STDOUT_FILENO, &barra1, 1);

    char pid_str[longitud];
    integerToString(pid, pid_str);
    write(STDOUT_FILENO, pid_str, (size_t) longitud);         //TODO: Aqui veo el fallo

    char * barra2 = "]\n";
    write(STDOUT_FILENO, barra2, 2);
}

int internal_cmd(struct execcmd * ecmd) {
    if (ecmd->argv[0] == 0) return 0;
    if (strcmp(ecmd->argv[0], "exit") == 0) {
        return 1;
    } else if (strcmp(ecmd->argv[0], "cwd") == 0) {
        return 2;
    } else if (strcmp(ecmd->argv[0], "cd") == 0) {
        return  3;
    } else  if (strcmp(ecmd->argv[0], "psplit") == 0) {
        return 4;
    } else if (strcmp(ecmd->argv[0], "bjobs") == 0) {
        return 5;
    }
    return 0;
}


void run_bjobs(int argc, char *pString[16]);

void exec_internal_cmd(struct execcmd * ecmd) {
    if (strcmp(ecmd->argv[0], "exit") == 0) {
        run_exit(ecmd);
        return;
    } else if (strcmp(ecmd->argv[0], "cwd") == 0) {
        run_cwd();
    } else if (strcmp(ecmd->argv[0], "cd") == 0) {
        if (ecmd->argv[1] != NULL) {
            if(ecmd->argc > 2) {
                printf("run_cd: Demasiados argumentos\n");
            } else {
                run_cd(ecmd->argv[1]);
            }
        } else {
            run_cd_HOME();
        }
    } else if (strcmp(ecmd->argv[0], "psplit") == 0) {
        run_psplit(ecmd->argc, ecmd->argv);
        nuevo_comando = 0;
    } else if (strcmp(ecmd->argv[0], "bjobs") == 0) {
        run_bjobs(ecmd->argc, ecmd->argv);
    }
}


/**
 *
 * @param argc
 * @param argv
 */
void run_bjobs(int argc, char* argv[]) {
    int opt;
    optind = 1;
    int hay_flags = 0;
    while ((opt = getopt(argc, argv, "kh")) != -1) {
        switch (opt) {
            case 'k':
                if (hay_flags > 0) {
                    perror("bjobs: Opciones incompatibles\n");
                    return;
                } else {
                    hay_flags++;
                    for (int i = 0; i < MAX_PROCS; ++i) {
                        if (is_back_process(pids_procesos[i]) == 0) {
                            kill(pids_procesos[i], SIGKILL);
                        }
                    }
                }
                break;
            case 'h':
                if (hay_flags > 0) {
                    perror("bjobs: Opciones incompatibles\n");
                    return;
                } else {
                    printf("Uso: bjobs [-k] [-h]\n"
                                   "\tOpciones:\n"
                                   "\t-k Mata todos los procesos en segundo plano.\n"
                                   "\t-h Ayuda\n");
                    return;
                }
        }


    }

    if (hay_flags == 0) {
        for (int i = 0; i < MAX_PROCS; ++i) {               // TODO: Un momento O.o
            if (pids_procesos[i] != -1)
                printf("[%d]\n", pids_procesos[i]);
        }
    }


}



//*******************************************************************

// `peek` recibe un puntero al principio de una cadena (`start_of_str`), otro
// puntero al final de esa cadena (`end_of_str`) y un conjunto de caracteres
// (`delimiter`).
//
// El primer puntero pasado como parámero (`start_of_str`) avanza hasta el
// primer carácter que no está en el conjunto de caracteres `WHITESPACE`.
//
// `peek` devuelve un tam_chunk_bytes distinto de `NULL` si encuentra alguno de los
// caracteres en `delimiter` justo después de los caracteres en `WHITESPACE`.

int peek(char** start_of_str, char* end_of_str, char* delimiter)
{
    char* s;

    s = *start_of_str;
    while (s < end_of_str && strchr(WHITESPACE, *s))
        s++;
    *start_of_str = s;

    return *s && strchr(delimiter, *s);
}


// Definiciones adelantadas de funciones
struct cmd* parse_line(char**, char*);
struct cmd* parse_pipe(char**, char*);
struct cmd* parse_exec(char**, char*);
struct cmd* parse_subs(char**, char*);
struct cmd* parse_redr(struct cmd*, char**, char*);
struct cmd* null_terminate(struct cmd*);


// `parse_cmd` realiza el *análisis sintáctico* de la línea de órdenes
// introducida por el usuario.
//
// `parse_cmd` utiliza `parse_line` para obtener una estructura `cmd`.

struct cmd* parse_cmd(char* start_of_str)
{
    char* end_of_str;
    struct cmd* cmd;

    DPRINTF(DBG_TRACE, "STR\n");

    end_of_str = start_of_str + strlen(start_of_str);

    cmd = parse_line(&start_of_str, end_of_str);

    // Comprueba que se ha alcanzado el final de la línea de órdenes
    peek(&start_of_str, end_of_str, "");
    if (start_of_str != end_of_str)
        error("%s: error sintáctico: %s\n", __func__);

    DPRINTF(DBG_TRACE, "END\n");

    return cmd;
}


// `parse_line` realiza el análisis sintáctico de la línea de órdenes
// introducida por el usuario.
//
// `parse_line` comprueba en primer lugar si la línea contiene alguna tubería.
// Para ello `parse_line` llama a `parse_pipe` que a su vez verifica si hay
// bloques de órdenes y/o redirecciones.  A continuación, `parse_line`
// comprueba si la ejecución de la línea se realiza en segundo plano (con `&`)
// o si la línea de órdenes contiene una lista de órdenes (con `;`).

struct cmd* parse_line(char** start_of_str, char* end_of_str)
{
    struct cmd* cmd;
    int delimiter;

    cmd = parse_pipe(start_of_str, end_of_str);

    while (peek(start_of_str, end_of_str, "&"))
    {
        // Consume el delimitador de tarea en segundo plano
        delimiter = get_token(start_of_str, end_of_str, 0, 0);
        assert(delimiter == '&');

        // Construye el `cmd` para la tarea en segundo plano
        cmd = backcmd(cmd);
    }

    if (peek(start_of_str, end_of_str, ";"))
    {
        if (cmd->type == EXEC && ((struct execcmd*) cmd)->argv[0] == 0)
            error("%s: error sintáctico: no se encontró comando\n", __func__);

        // Consume el delimitador de lista de órdenes
        delimiter = get_token(start_of_str, end_of_str, 0, 0);
        assert(delimiter == ';');

        // Construye el `cmd` para la lista
        cmd = listcmd(cmd, parse_line(start_of_str, end_of_str));
    }

    return cmd;
}


// `parse_pipe` realiza el análisis sintáctico de una tubería de manera
// recursiva si encuentra el delimitador de tuberías '|'.
//
// `parse_pipe` llama a `parse_exec` y `parse_pipe` de manera recursiva para
// realizar el análisis sintáctico de todos los componentes de la tubería.

struct cmd* parse_pipe(char** start_of_str, char* end_of_str)
{
    struct cmd* cmd;
    int delimiter;

    cmd = parse_exec(start_of_str, end_of_str);

    if (peek(start_of_str, end_of_str, "|"))
    {
        if (cmd->type == EXEC && ((struct execcmd*) cmd)->argv[0] == 0)
            error("%s: error sintáctico: no se encontró comando\n", __func__);

        // Consume el delimitador de tubería
        delimiter = get_token(start_of_str, end_of_str, 0, 0);
        assert(delimiter == '|');

        // Construye el `cmd` para la tubería
        cmd = pipecmd(cmd, parse_pipe(start_of_str, end_of_str));
    }

    return cmd;
}


// `parse_exec` realiza el análisis sintáctico de un comando a no ser que la
// expresión comience por un paréntesis, en cuyo caso se llama a `parse_subs`.
//
// `parse_exec` reconoce las redirecciones antes y después del comando.

struct cmd* parse_exec(char** start_of_str, char* end_of_str)
{
    char* start_of_token;
    char* end_of_token;
    int token, argc;
    struct execcmd* cmd;
    struct cmd* ret;

    // ¿Inicio de un bloque?        Si empieza por '(' encot
    if (peek(start_of_str, end_of_str, "("))
        return parse_subs(start_of_str, end_of_str);

    // Si no, lo primero que hay en una línea de órdenes es un comando

    // Construye el `cmd` para el comando
    ret = execcmd();
    cmd = (struct execcmd*) ret;

    // ¿Redirecciones antes del comando?
    ret = parse_redr(ret, start_of_str, end_of_str);

    // Bucle para separar los argumentos de las posibles redirecciones
    argc = 0;
    while (!peek(start_of_str, end_of_str, "|)&;"))
    {
        if ((token = get_token(start_of_str, end_of_str,
                               &start_of_token, &end_of_token)) == 0)
            break;

        // El siguiente token debe ser un argumento porque el bucle
        // para en los delimitadores
        if (token != 'a')
            error("%s: error sintáctico: se esperaba un argumento\n", __func__);

        // Almacena el siguiente argumento reconocido. El primero es
        // el comando
        cmd->argv[argc] = start_of_token;
        cmd->eargv[argc] = end_of_token;
        cmd->argc = ++argc;
        if (argc >= MAX_ARGS)
            panic("%s: demasiados argumentos\n", __func__);

        // ¿Redirecciones después del comando?
        ret = parse_redr(ret, start_of_str, end_of_str);
    }

    // El comando no tiene más parámetros
    cmd->argv[argc] = 0;
    cmd->eargv[argc] = 0;

    return ret;
}


// `parse_subs` realiza el análisis sintáctico de un bloque de órdenes
// delimitadas por paréntesis o `subshell` llamando a `parse_line`.
//
// `parse_subs` reconoce las redirecciones después del bloque de órdenes.

struct cmd* parse_subs(char** start_of_str, char* end_of_str)
{
    int delimiter;
    struct cmd* cmd;
    struct cmd* scmd;

    // Consume el paréntesis de apertura
    if (!peek(start_of_str, end_of_str, "("))
        error("%s: error sintáctico: se esperaba '('", __func__);
    delimiter = get_token(start_of_str, end_of_str, 0, 0);
    assert(delimiter == '(');

    // Realiza el análisis sintáctico hasta el paréntesis de cierre
    scmd = parse_line(start_of_str, end_of_str);

    // Construye el `cmd` para el bloque de órdenes
    cmd = subscmd(scmd);

    // Consume el paréntesis de cierre
    if (!peek(start_of_str, end_of_str, ")"))
        error("%s: error sintáctico: se esperaba ')'", __func__);
    delimiter = get_token(start_of_str, end_of_str, 0, 0);
    assert(delimiter == ')');

    // ¿Redirecciones después del bloque de órdenes?
    cmd = parse_redr(cmd, start_of_str, end_of_str);

    return cmd;
}


// `parse_redr` realiza el análisis sintáctico de órdenes con
// redirecciones si encuentra alguno de los delimitadores de
// redirección ('<' o '>').

struct cmd* parse_redr(struct cmd* cmd, char** start_of_str, char* end_of_str)
{
    int delimiter;
    char* start_of_token;
    char* end_of_token;

    // Si lo siguiente que hay a continuación es delimitador de
    // redirección...
    while (peek(start_of_str, end_of_str, "<>"))
    {
        // Consume el delimitador de redirección
        delimiter = get_token(start_of_str, end_of_str, 0, 0);
        assert(delimiter == '<' || delimiter == '>' || delimiter == '+');

        // El siguiente token tiene que ser el nombre del fichero de la
        // redirección entre `start_of_token` y `end_of_token`.
        if ('a' != get_token(start_of_str, end_of_str, &start_of_token, &end_of_token))
            error("%s: error sintáctico: se esperaba un fichero", __func__);

        // Construye el `cmd` para la redirección
        switch(delimiter)
        {
            case '<':
                cmd = redrcmd(cmd, start_of_token, end_of_token, O_RDONLY, S_IRWXU, STDIN_FILENO);
                break;
            case '>':
                cmd = redrcmd(cmd, start_of_token, end_of_token, O_RDWR|O_CREAT|O_TRUNC, S_IRWXU, STDOUT_FILENO);     //
                break;
            case '+': // >>
                cmd = redrcmd(cmd, start_of_token, end_of_token, O_RDWR|O_CREAT|O_APPEND, S_IRWXU, STDOUT_FILENO);
                break;
        }
    }

    return cmd;
}


// Termina en NULL todas las cadenas de las estructuras `cmd`
struct cmd* null_terminate(struct cmd* cmd)
{
    struct execcmd* ecmd;
    struct redrcmd* rcmd;
    struct pipecmd* pcmd;
    struct listcmd* lcmd;
    struct backcmd* bcmd;
    struct subscmd* scmd;
    int i;

    if(cmd == 0)
        return 0;

    switch(cmd->type)
    {
        case EXEC:
            ecmd = (struct execcmd*) cmd;
            for(i = 0; ecmd->argv[i]; i++)
                *ecmd->eargv[i] = 0;
            break;

        case REDR:
            rcmd = (struct redrcmd*) cmd;
            null_terminate(rcmd->cmd);
            *rcmd->efile = 0;
            break;

        case PIPE:
            pcmd = (struct pipecmd*) cmd;
            null_terminate(pcmd->left);
            null_terminate(pcmd->right);
            break;

        case LIST:
            lcmd = (struct listcmd*) cmd;
            null_terminate(lcmd->left);
            null_terminate(lcmd->right);
            break;

        case BACK:
            bcmd = (struct backcmd*) cmd;
            null_terminate(bcmd->cmd);
            break;

        case SUBS:
            scmd = (struct subscmd*) cmd;
            null_terminate(scmd->cmd);
            break;

        case INV:
        default:
            panic("%s: estructura `cmd` desconocida\n", __func__);
    }

    return cmd;
}


/******************************************************************************
 * Funciones para la ejecución de la línea de órdenes
 ******************************************************************************/


void exec_cmd(struct execcmd* ecmd)
{
    assert(ecmd->type == EXEC);
    if (ecmd->argv[0] == 0) exit(EXIT_SUCCESS);
    if (internal_cmd(ecmd) > 0) {
        exec_internal_cmd(ecmd);
    } else {
        execvp(ecmd->argv[0], ecmd->argv);
        panic("no se encontró el comando xd '%s'\n", ecmd->argv[0]);
    }
}


void bloquear_sigchild() {
    if (sigprocmask(SIG_BLOCK, &signal_child, NULL) == -1) {
        perror(" sigprocmask SIG_BLOCK ");
        exit(EXIT_FAILURE);
    }
}

void release_sigchild() {
    if (sigprocmask(SIG_UNBLOCK, &signal_child, NULL) == -1) {
        perror(" sigprocmask SIG_UNBLOCK ");
        exit(EXIT_FAILURE);
    }
}

void free_cmd(struct cmd* cmd)
{
    struct execcmd* ecmd;
    struct redrcmd* rcmd;
    struct listcmd* lcmd;
    struct pipecmd* pcmd;
    struct backcmd* bcmd;
    struct subscmd* scmd;

    if(cmd == 0) return;

    switch(cmd->type)
    {
        case EXEC:
            // free(ecmd);
            break;

        case REDR:
            rcmd = (struct redrcmd*) cmd;
            free_cmd(rcmd->cmd);

            free(rcmd->cmd);
            break;

        case LIST:
            lcmd = (struct listcmd*) cmd;

            free_cmd(lcmd->left);
            free_cmd(lcmd->right);

            free(lcmd->right);
            free(lcmd->left);
            break;

        case PIPE:
            pcmd = (struct pipecmd*) cmd;

            free_cmd(pcmd->left);
            free_cmd(pcmd->right);

            free(pcmd->right);
            free(pcmd->left);
            break;

        case BACK:
            bcmd = (struct backcmd*) cmd;

            free_cmd(bcmd->cmd);

            free(bcmd->cmd);
            break;

        case SUBS:
            scmd = (struct subscmd*) cmd;

            free_cmd(scmd->cmd);

            free(scmd->cmd);
            break;

        case INV:
        default:
            panic("%s: estructura `cmd` desconocida\n", __func__);
    }
}

void run_cmd(struct cmd* cmd)
{
    struct execcmd* ecmd;
    struct redrcmd* rcmd;
    struct listcmd* lcmd;
    struct pipecmd* pcmd;
    struct backcmd* bcmd;
    struct subscmd* scmd;
    int p[2];
    int fd;
    pid_t pid;

    DPRINTF(DBG_TRACE, "STR\n");
    if(cmd == 0) return;

    switch(cmd->type)
    {
        case EXEC:
            ecmd = (struct execcmd*) cmd;
            if (internal_cmd(ecmd) > 0) {
                exec_internal_cmd(ecmd);
            } else {
                bloquear_sigchild();
                if ((pid = fork_or_panic("fork EXEC")) == 0)
                    exec_cmd(ecmd);
                TRY(waitpid(pid, NULL, 0));
                release_sigchild();
            }
            break;

        case REDR:
            rcmd = (struct redrcmd*) cmd;
            int terminal_fd = dup(STDOUT_FILENO);
            //close(rcmd->fd);
            int es_cmd_interno = 0;
            if (rcmd->cmd->type == EXEC)
                es_cmd_interno = internal_cmd((struct execcmd*) rcmd->cmd);

            if(es_cmd_interno > 0) {
                close(rcmd->fd);
                if ((fd = open(rcmd->file, rcmd->flags, rcmd->mode)) < 0) {
                    perror("open");
                    exit(EXIT_FAILURE);
                }
                if (es_cmd_interno == 1) {
                    close(fd);
                    dup2(terminal_fd, fd);
                    close(terminal_fd);
                    free(rcmd->cmd);
                    free(cmd);
                    exit(EXIT_SUCCESS);
                }else {
                    exec_internal_cmd((struct execcmd *) rcmd->cmd);
                    close(fd);
                    dup2(terminal_fd, STDOUT_FILENO);
                    close(terminal_fd);
                }
            } else {
                if ((pid = fork_or_panic("fork REDR")) == 0) {
                    TRY(close(rcmd->fd));
                    if ((fd = open(rcmd->file, rcmd->flags, rcmd->mode)) < 0) {
                        perror("open");
                        exit(EXIT_FAILURE);
                    }

                    if (rcmd->cmd->type == EXEC) {
                        exec_cmd((struct execcmd *) rcmd->cmd);
                    } else
                        run_cmd(rcmd->cmd);
                    exit(EXIT_SUCCESS);
                }
                if (is_back_process(pid) != 0) {
                    bloquear_sigchild();
                    TRY(waitpid(pid, NULL, 0));
                    release_sigchild();
                }
                //TRY(wait(NULL));
            }
            break;

        case LIST:
            lcmd = (struct listcmd*) cmd;
            run_cmd(lcmd->left);
            if (es_exit == 1) {
                return;
            }
            run_cmd(lcmd->right);
            break;

        case PIPE:
            pcmd = (struct pipecmd*)cmd;
            if (pipe(p) < 0)
            {
                perror("pipe");
                exit(EXIT_FAILURE);
            }

            bloquear_sigchild();
            // Ejecución del hijo de la izquierda
            if ((pid = fork_or_panic("fork PIPE left")) == 0)
            {
                TRY( close(1) );
                TRY( dup(p[1]) );
                TRY( close(p[0]) );
                TRY( close(p[1]) );
                if (pcmd->left->type == EXEC)
                    exec_cmd((struct execcmd*) pcmd->left);
                else
                    run_cmd(pcmd->left);
                exit(EXIT_SUCCESS);
            }
            if (es_exit == 1) {
                return;
            }
            pid_t pid2;


            // Ejecución del hijo de la derecha
            if ((pid2 = fork_or_panic("fork PIPE right")) == 0)
            {
                TRY( close(0) );
                TRY( dup(p[0]) );
                TRY( close(p[0]) );
                TRY( close(p[1]) );
                if (pcmd->right->type == EXEC)
                    exec_cmd((struct execcmd*) pcmd->right);
                else
                    run_cmd(pcmd->right);
                exit(EXIT_SUCCESS);
            }
            TRY( close(p[0]) );
            TRY( close(p[1]) );

            // Esperar a ambos hijos
            TRY( waitpid(pid, NULL, 0) );          // TODO FALLA AQUI :S
            TRY( waitpid(pid2, NULL, 0) );
            release_sigchild();
            break;

        case BACK:
            bcmd = (struct backcmd*)cmd;
            bloquear_sigchild();
            if ((pid = fork_or_panic("fork BACK")) == 0)
            {
                if (bcmd->cmd->type == EXEC)
                    exec_cmd((struct execcmd*) bcmd->cmd);
                else
                    run_cmd(bcmd->cmd);
                exit(EXIT_SUCCESS);
            }
            int contador = 0;

            while ((contador < MAX_PROCS) && (pids_procesos[contador] != -1)) {
                contador++;
            }

            if (pids_procesos[contador] == -1) {
                pids_procesos[contador] = pid;
            }

            // Imprimir procesos
            /*
             *
             */
            print_processid(pid);
            release_sigchild();

            break;

        case SUBS:
            scmd = (struct subscmd*) cmd;
            bloquear_sigchild();
            if ((pid = fork_or_panic("fork SUBS")) == 0)
            {
                run_cmd(scmd->cmd);
                exit(EXIT_SUCCESS);
            }
            TRY( waitpid(pid, NULL, 0) );
            release_sigchild();

            break;

        case INV:
            break;
        default:
            panic("%s: estructura `cmd` desconocida\n", __func__);
            break;
    }

    DPRINTF(DBG_TRACE, "END\n");
}


void print_cmd(struct cmd* cmd)
{
    struct execcmd* ecmd;
    struct redrcmd* rcmd;
    struct listcmd* lcmd;
    struct pipecmd* pcmd;
    struct backcmd* bcmd;
    struct subscmd* scmd;

    if(cmd == 0) return;

    switch(cmd->type)
    {
        default:
            panic("%s: estructura `cmd` desconocida\n", __func__);

        case EXEC:
            ecmd = (struct execcmd*) cmd;
            if (ecmd->argv[0] != 0)
                printf("fork( exec( %s ) )", ecmd->argv[0]);
            break;

        case REDR:
            rcmd = (struct redrcmd*) cmd;
            printf("fork( ");
            if (rcmd->cmd->type == EXEC)
                printf("exec ( %s )", ((struct execcmd*) rcmd->cmd)->argv[0]);
            else
                print_cmd(rcmd->cmd);
            printf(" )");
            break;

        case LIST:
            lcmd = (struct listcmd*) cmd;
            print_cmd(lcmd->left);
            printf(" ; ");
            print_cmd(lcmd->right);
            break;

        case PIPE:
            pcmd = (struct pipecmd*) cmd;
            printf("fork( ");
            if (pcmd->left->type == EXEC)
                printf("exec ( %s )", ((struct execcmd*) pcmd->left)->argv[0]);
            else
                print_cmd(pcmd->left);
            printf(" ) => fork( ");
            if (pcmd->right->type == EXEC)
                printf("exec ( %s )", ((struct execcmd*) pcmd->right)->argv[0]);
            else
                print_cmd(pcmd->right);
            printf(" )");
            break;

        case BACK:
            bcmd = (struct backcmd*) cmd;
            printf("fork( ");
            if (bcmd->cmd->type == EXEC)
                printf("exec ( %s )", ((struct execcmd*) bcmd->cmd)->argv[0]);
            else
                print_cmd(bcmd->cmd);
            printf(" )");
            break;

        case SUBS:
            scmd = (struct subscmd*) cmd;
            printf("fork( ");
            print_cmd(scmd->cmd);
            printf(" )");
            break;
    }
}


/******************************************************************************
 * Lectura de la línea de órdenes con la biblioteca libreadline
 ******************************************************************************/


// `get_cmd` muestra un *prompt* y lee lo que el usuario escribe usando la
// biblioteca readline. Ésta permite mantener el historial, utilizar las flechas
// para acceder a las órdenes previas del historial, búsquedas de órdenes, etc.

char* get_cmd()
{
    char* buf;

    uid_t uid = getuid();
    struct passwd *pw = getpwuid(uid);
    if (pw == NULL) {
        perror("getpwuid");
        exit(EXIT_FAILURE);
    }
    char ruta[PATH_MAX];
    if (!getcwd(ruta, PATH_MAX)) {
        perror("FALLO DE RUTA");
        exit(EXIT_FAILURE);
    }
    //run_cwd();
    char * dir_actual = basename(ruta);                                     // TODO: Comprobar si la funcion lanza excepciones
    size_t prompt_size = strlen(pw->pw_name) + strlen(dir_actual) + 4;
    char *prompt;
    prompt = malloc(prompt_size * sizeof(char));
    sprintf(prompt, "%s@%s> ", pw->pw_name, dir_actual);

    // Lee la orden tecleada por el usuario
    buf = readline(prompt);

    free(prompt);
    // Si el usuario ha escrito una orden, almacenarla en la historia.
    if(buf)
        add_history(buf);

    return buf;
}


/******************************************************************************
 * Bucle principal de `simplesh`
 ******************************************************************************/


void help(int argc, char **argv)
{
    info("Usage: %s [-d N] [-h]\n\
         shell simplesh v%s\n\
         Options: \n\
         -d set debug level to N\n\
         -h help\n\n",
         argv[0], VERSION);
}


void parse_args(int argc, char** argv)
{
    int option;

    // Bucle de procesamiento de parámetros
    while((option = getopt(argc, argv, "d:h")) != -1) {
        switch(option) {
            case 'd':
                g_dbg_level = atoi(optarg);
                break;
            case 'h':
            default:
                help(argc, argv);
                exit(EXIT_SUCCESS);
                break;
        }
    }
}


// MANEJADOR PARA sig_child
void handle_sig_child(int signal) {
    int saved_errno = errno;
    int pid;
    while ((pid = waitpid((pid_t)(-1), 0, WNOHANG)) > 0) {
        for (int i = 0; i < MAX_PROCS; ++i) {
            if (pid == pids_procesos[i]) {
                //   bloquear_sigchild();
                pids_procesos[i] = -1;
                //   release_sigchild();
                break;
            }
        }
        print_processid(pid);
    }
    errno = saved_errno;
}


int main(int argc, char** argv)
{
    char* buf;
    struct cmd* cmd;

    // AQUI DEBEMOS INICIALIZAR LOS PID


    memset(pids_procesos,-1,(sizeof (*pids_procesos)*MAX_PROCS));
    // memset(secondProcess, -1, MAXBUF);
    for (int i = 0; i < MAX_PROCS; i++){
        pids_procesos[i]= -1;
    }


    sigemptyset(&signal_child);
    sigaddset(&signal_child, SIGCHLD);


    // Bloquear la señal SIGINT

    sigset_t  blocked_sigint;
    sigemptyset(&blocked_sigint);
    sigaddset(&blocked_sigint, SIGINT);
    if (sigprocmask(SIG_BLOCK, &blocked_sigint, NULL) == -1) {
        perror("SIGPROCKMASK: SIGNIT");
        exit(EXIT_FAILURE);
    }

    // ignorar la señal

    /*sigset_t ignored_sigint;
    sigemptyset(&ignored_sigint);
    sigaddset(&ignored_sigint, SIGQUIT);
    if (sigprocmask(SIG_UNBLOCK, &ignored_sigint, NULL) == -1) {
        perror("SIGPROCMASK: SIGQUIT");
        exit(EXIT_FAILURE);
    }*/

    struct sigaction sa1;
    sa1.sa_handler = SIG_IGN;
    sigemptyset(&sa1.sa_mask);
    sa1.sa_flags = 0;
    if (sigaction(SIGCHLD, &sa1, 0) == -1) {
        perror(0);
        exit(1);
    }


    struct sigaction sa;
    memset(&sa, 0, sizeof(sa));
    sa.sa_handler = &handle_sig_child;
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = SA_RESTART | SA_NOCLDSTOP;
    if (sigaction(SIGCHLD, &sa, NULL) == -1) {
        perror(0);
        exit(EXIT_FAILURE);
    }

    parse_args(argc, argv);


    DPRINTF(DBG_TRACE, "STR\n");

    // Bucle de lectura y ejecución de órdenes
    while ((buf = get_cmd()) != NULL)
    {

        // Realiza el análisis sintáctico de la línea de órdenes
        cmd = parse_cmd(buf);

        // Termina en `NULL` todas las cadenas de las estructuras `cmd`
        null_terminate(cmd);

        DBLOCK(DBG_CMD, {
            info("%s:%d:%s: print_cmd: ",
                 __FILE__, __LINE__, __func__);
            print_cmd(cmd); printf("\n"); fflush(NULL); } );


        // Ejecuta la línea de órdenes
        run_cmd(cmd);


        // Libera la memoria de las estructuras `cmd`
        free_cmd(cmd);
        free(cmd);
        // Libera la memoria de la línea de órdenes
        free(buf);

        if (es_exit == 1)
            exit(EXIT_SUCCESS);

    }


    DPRINTF(DBG_TRACE, "END\n");
    return 0;
}

