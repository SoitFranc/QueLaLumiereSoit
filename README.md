#QueLaLumiereSoit

##Quelques pratiques utiles :

**Changer la forme du terrain :**
Changer la forme du terrain concerne plusieurs changements :
- au niveau de l'IHM : RDV dans la classe TerrainForm si vous voulez changer Ti et Tr, ou dans ToForm si vous voulez changer To.
- au niveau AMAS : l'agent doit conna�tre �galement le terrain sur lequel il peut se d�placer. 
	- amak.MyAMAS#onInitialConfiguration : (cr�ation des 1ers agents � une coordonn�e al�atoire dans TO) il faut changer l'instruction "double[] coo = genererCoordonneeCercle();"
	- amak.MyEnvironment#isValideInTo ou amak.MyEnvironment#isValideInTi : retourne vrai si la coordon�e donn�e en param�tre n'est pas hors-map.



