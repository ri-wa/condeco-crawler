
# Condeco-Crawler
Ermöglicht es automatisch Sitzplätze in Condeco zu buchen. Die Notwendigen Configurationen werden in Firestore gespeichert.  
In der config/Condeco.kt werden die notwendigen Parameter gepflegt, damit klar ist, wo die Config zu finden ist.  
In de Sammlung müssen zwei Dokumente enthalten sein:

- credentials:
    - username: String
    - password: String
- configuration:
    - baseUrl: String
    - weekDays:  IntArray (0 = Sonntag, 1 = Montag ...)
    - excludedDates: StringArray (01/01/2025 z.b.)
    - locationID: Int
    - groupID: Int
    - baseUrl: String
    - singleWorkplaces: StringArray ("Desk 503" z.b.)
    - preferredDeskId: Int
    - preferredFloorId: Int
    - floors: IntArray

## Dockerfile
Das Projekt lässt sich als dockerfile bauen und in google in die artifact registry speichern
- docker file bauen: ` docker build -t $TAG_NAME .`
- docker file für artifact registry taggen: `docker tag $TAG_NAME $PATH_TO_REGISTRY/image:$TAG`
- docker file in articat registry hochladen: `docker push $TAG_NAME $PATH_TO_REGISTRY/image:$TAG`

Anschließend kann es z.b. als Cronjob in Google cloud run laufen


