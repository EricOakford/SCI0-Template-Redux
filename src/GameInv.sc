;;; Sierra Script 1.0 - (do not remove this comment)
;
;	GAMEINV.SC
;
;	Here, you can add inventory item instances.
;	
;	 An example might be::
;	
;	 	(instance Hammer of InvItem
;	 		(properties
;	 			name	{Hammer}				;the literal name
;				said	'/hammer'				;said spec which user can type to identify this item
;				description	{It's hammer time!}	;long text description
;				owner							;who owns this item
;				view							;picture of the item
;				loop
;				cel
;				script							;a script that can control the item
;	 		)
;	 	)
;	
;	Then in the invCode init, add the inventory item to the add: call.
;
;	Although this could be in the MAIN script, the more items you add to the inventory, the more memory
;	that script will need. Putting the items here makes everything tidier. In fact, this was done for
;	Codename Iceman and Conquests of Camelot.
;	
;
;

(script# GAME_INV)
(include game.sh)
(use Main)
(use Invent)
(use System)

(public
	invCode 0
)

(instance invCode of Code
	(method (init)
		(inventory add:
			;Add your inventory items here. Make sure they are in the same order as the item list in GAME.SH.
				Test_Object
		)
	)
)

(instance Test_Object of InvItem
	(properties
		name {Test Object}
		description {This is a test object.}
		owner 0
		view vTestObject
		loop 0
		cel 0
	)
)
