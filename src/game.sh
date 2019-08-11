;;; Sierra Script 1.0 - (do not remove this comment)
;**************************************************************
;***
;***	GAME.SH--
;***
;**************************************************************


(include pics.sh) (include views.sh) ;graphical defines
(include system.sh) (include sci2.sh) ;system and kernel functions

;Game modules
(enum
	MAIN		;0
	SPEEDTEST	;1
	DEBUG		;2
	GAME_INV	;3
	GAME_INIT	;4
)

;Actual rooms
(enum 10
	TITLE		;10
	TESTROOM	;11
)

;Sound defines
(define sTitle 1)
(define sDeath 5)

;Inventory items
(enum
	iTestObject
)