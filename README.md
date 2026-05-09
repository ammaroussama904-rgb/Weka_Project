Ce projet est une application Java complète permettant d'entraîner une Intelligence Artificielle à reconnaître des chiffres écrits à la main (0-9) en utilisant l'algorithme Random Forest de la bibliothèque Weka.
Performance du Modèle
Précision (Accuracy) : 96.68% sur le jeu de test.

Dataset : MNIST (60,000 images d'entraînement, 12,000 de test).

Algorithme : Random Forest (200 arbres de décision).

 Structure du Projet
Le projet est divisé en quatre grandes étapes :

Chargement des données (MnistLoader.java) : Lecture des fichiers binaires originaux (.idx3-ubyte).

Conversion ARFF (MnistToArff.java) : Transformation des images en format texte compatible avec Weka (valeurs de pixels de 0 à 255).

Entraînement (WekaClassifier.java) : Construction et sauvegarde du modèle .model.

Interface de dessin (MnistDrawingGUI.java) : Application interactive pour tester l'IA en temps réel.

 Prétraitement de l'Image (Le secret de la précision)
Pour que l'IA comprenne bien ton dessin à la souris, nous appliquons trois étapes de nettoyage automatique avant chaque prédiction :

Épaississement : Simulation d'un tracé au feutre.

Bounding Box : Détection des limites du chiffre dessiné.

Centrage : Repositionnement du chiffre au centre exact du carré 28x28 (comme dans les données MNIST originales).

 Installation et Lancement
Assurez-vous que les fichiers mnist_train.arff et mnist_rf.model sont à la racine du projet.

Ajoutez les bibliothèques Weka.jar au Build Path de votre projet Eclipse/IntelliJ.

Lancez la classe MnistDrawingGUI.java.

Si le programme met du temps à s'ouvrir, augmentez la mémoire vive dans les arguments VM : -Xmx1024m.

📝 Note sur le développement
Le projet inclut également une implémentation manuelle de l'algorithme k-NN (k-plus proches voisins) avec calcul de distance euclidienne pour comparer les performances avec Weka.
