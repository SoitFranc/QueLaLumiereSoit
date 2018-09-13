#QueLaLumiereSoit

##Quelques pratiques utiles :

**Changer la forme du terrain :**
Changer la forme du terrain concerne plusieurs changements :
- au niveau de l'IHM : RDV dans la classe TerrainForm si vous voulez changer Ti et Tr, ou dans ToForm si vous voulez changer To.
- au niveau AMAS : l'agent doit connaître également le terrain sur lequel il peut se déplacer. 
	- amak.MyAMAS#onInitialConfiguration : (création des 1ers agents à une coordonnée aléatoire dans TO) il faut changer l'instruction "double[] coo = genererCoordonneeCercle();"
	- amak.MyEnvironment#isValideInTo ou amak.MyEnvironment#isValideInTi : retourne vrai si la coordonée donnée en paramètre n'est pas hors-map.



