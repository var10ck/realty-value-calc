# Инструкция по сборке и развертыванию

## Развертывание с помощью Docker

### Развертывание готового образа с помощью docker-compose

Чтобы развернуть систему в Docker понадобится:

- [Docker](https://docs.docker.com/engine/install/)
- Утилита [docker-compose](https://docker-docs.uclv.cu/compose/install/) (Поставляется вместе с Docker Desktop для
  Windows и Mac)
- Файлы [docker-compose.yml](../../docker-compose.yml) и [.env](../../.env), расположенный в корне проекта.

Откройте терминал, перейдите в директорию, в которой расположен [docker-compose.yml](../../docker-compose.yml) и
выполните команду:

```console
foo@bar:~$ docker-compose up -d

Creating network "realty-value-calc_realty-value-calc-network" with the default driver
Creating realty-value-calc_realty-value-calc-backend_1 ... done
Creating realty-value-calc_postgres_1                  ... done
```

Docker загрузит необходимые образы контейнеров из реестра DockerHub, создаст и запустит контейнеры с параметрами, 
установленными в файле [.env](../../.env)

#### Изменение параметров конфигурации

Файл [.env](../../.env) содержит следующие параметры конфигурации:

```lombok.config
#Хост, на котором запустится приложение
API_HOST=localhost
#Порт, на котором запустится приложение
API_PORT=8080

#Конфигурация базы данных: host, port, название создаваемой базы данных, пользователь и пароль для создаваемой базы
# DB_HOST будет использоваться в качестве container_name для Postgres
DB_HOST=postgres
DB_PORT=5433
DB_NAME=portal
DB_USER=postgres
DB_PASSWORD=postgres
```

Данные параметры можно изменить, изменяя содержимое файла или установив соответствующие 
переменные окружения в оперционной системе, в таком случае значения из окружения переопределят 
значения из файла.

### Сборка собственного образа

> :Warning: Описана сборка образа контейнера бэкэнда системы, СУБД 
> должна быть развернута отдельно, параметры для доступа к БД необходимо прокинуть 
> через переменные среды

Для сборки образа понадобится установить [SBT](https://www.scala-sbt.org/) - инструмент для сборки для Scala.

В директории с проектом выполнить:
```console
foo@bar:~$ sbt update
```

Данная команда загрузит необходимую версию Scala и остальные зависимости проекта.

Для создания образа выполните:
```console
foo@bar:~$ sbt docker:publishLocal
```

После этого sbt сгенерирует скомпилирует исходники, сгенерирует Dockerfile и создаст образ контейнера.

Посмотреть список контейнеров на устройстве можно так:
```console
foo@bar:~$ docker images
```
![docker images result example](./assets/docker-images-assets.png)

> :Warning: Для корректного запуска контейнера обязательно трубуется передать переменные
> окружения DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD

После сборки конетейнера его можно запустить:
```console
foo@bar:~$ docker run -p 8080:8080 -d -ti -e API_HOST='localhost' \
-e API_PORT=8080
-e DB_HOST='postgres'
-e DB_PORT=5433
-e DB_NAME='portal'
-e DB_USER='postgres'
-e DB_PASSWORD='postgres'
--name container_name
realty-value-calc:0.1.1
```
В данном примере сервис доступен на порту 8080.

## Создание сборки для конкретной платформы

Для сборки также понадобится [SBT](https://www.scala-sbt.org/)

Для начала установить зависимости:
```console
foo@bar:~$ sbt update
```

С помощью SBT плагина, установленного в проекте, можно создать сборку 
под различные платформы: Docker(описано выше), Linux(Debian, RPM), Windows, GraalVM

### Пример сботки для Debian

В системе должны быть установлены следующие приложения:
* dpkg-deb
* dpkg-sig
* dpkg-genchanges
* lintian
* fakeroot

Для пакета Debian необходимы некоторые обязательные настройки. Убедитесь, что у вас есть эти настройки в build.sbt:
```scala
name := "Debian Example"

version := "1.0"

maintainer := "Max Smith <max.smith@yourcompany.io>"

packageSummary := "Hello World Debian Package"

packageDescription :=
        """A fun package description of our software,
          with multiple lines."""

debianPackageDependencies := Seq("java8-runtime-headless")

enablePlugins(DebianPlugin)
```

Запустите следующую команду в директории с проектом:
```console
foo@bar:~$ sbt debian:packageBin
```

После этого sbt соберет Deb-пакет в директории target/

Более подробную информацию о сборке под различные платформы можно найти в официальной документации плагина sbt-native packager:
https://www.scala-sbt.org/sbt-native-packager/formats/index.html
