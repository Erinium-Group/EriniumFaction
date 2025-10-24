# Format JSON Faction

## Boolean

- **isAdminFaction** = Boolean
- **openFaction** = Boolean
- **isWarzone** = Boolean
- **isSafezone** = Boolean

## String

- **displayname** = String
- **memberList** = String (uuid:rank, uuid:rank)
- **owner** = uuid
- **invitedPlayers** = String (uuid)
- **allies** = factionid
- **homeLocation** = String(int, int, int)
- **warps** = String(int, int, int/)
- **description** = String
- **upgrades** = String
- **claimlist** = String (region/chunk, etc.)
- **officer** = perm
- **member** = perm
- **recruit** = perm

## int / Double

- **power** = int
- **claim** = int
- **maxClaims** = int
- **maxPlayer** = int
- **dateOfCreation** = int
- **factionLevel** = int
- **factionXp** = int
- **bankBalance** = Double
- **maxWarps** = int

## Faction Permissions

### Interaction / Actions
- canBreak
- canPlace
- canInteract
- openChest

### Commandes
- canInvite
- canKick
- canPromote
- canDemote
- canHome
- canWarp

### Gestion
- ManageSettings
- canManageRanks
- canChangeDesc
- canClaim
- canUnclaim
- canSetHome
- canManageWarps
- canAlly

### Économie
- canWithdrawMoney
- canDepositMoney