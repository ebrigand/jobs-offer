# jobs-offer

**Test pour Hellowork**

Ce projet est une démonstration technique réalisée dans le cadre d'un test pour Hellowork.

## 🚀 Objectif

L'objectif principal de ce projet est de démontrer les compétences en développement Java et en intégration avec Gradle, tout en utilisant les meilleures pratiques de développement.

## 🛠️ Technologies utilisées

- **Langage** : Java 21
- **Build System** : Gradle
- **IDE** : IntelliJ IDEA
- **Contrôle de version** : Git
- **Base** : MongoDB Community 8.x
- **Techno** : Spring Boot / Reactor
- **Tests** : Jupiter / Mockito

## ✅ Pré requis

Java 21
MongoDB installée sur localhost avec le port 27017 d'ouvert et une base de créée: "jobsdb" avec la collection "offers"

## ✅ Instructions pour démarrer

1. Clonez ce dépôt sur votre machine locale :

   ```bash
   git clone https://github.com/ebrigand/jobs-offer.git
   cd jobs-offer
Assurez-vous d'avoir Gradle installé sur votre machine. Si ce n'est pas le cas, vous pouvez l'installer depuis https://gradle.org/install/.

Exécutez le projet avec Gradle :

   ```bash
./gradlew build
./gradlew bootRun
```
Sur Windows, utilisez :

   ```bash
gradlew.bat build
gradlew.bat bootRun
```
🧪 Tests
Les tests unitaires sont situés dans le répertoire src/test. Vous pouvez les exécuter avec la commande suivante :

   ```bash
./gradlew test -Dspring.profiles.active=test 
```

## ✅ Utilisation
Faire un POST avec cette URL:
http://localhost:8080/jobs-offer/store 

📝 Remarque: Appel non bloquant la réponse est retournée de suite (car si il y'a beaucoup d'offres un appel bloquant ne semble pas adapté)

Faire un GET avec cette URL:
http://localhost:8080/jobs-offer/report




`
