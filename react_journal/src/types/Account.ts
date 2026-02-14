export class Account {
  public id: number; 
  public userId: number; // For frontend use, maps to 'user' from backend if that's the PK
  public name: string;
  public initialBalance: number; // For frontend internal state
  public currentBalance: number; // For frontend internal state
  public createdAt?: string; 
  public updatedAt?: string;

  constructor(
    id: number,
    userId: number, 
    name: string,
    initialBalance: number,
    currentBalance: number,
    createdAt?: string,
    updatedAt?: string
  ) {
    this.id = id;
    this.userId = userId; 
    this.name = name;
    this.initialBalance = initialBalance; // Corresponds to backend's initial_balance after mapping
    this.currentBalance = currentBalance; // Corresponds to backend's current_balance after mapping
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }
}

// This type defines what the FRONTEND SENDS to the backend for CREATION
export type CreateAccountPayload = {
  name: string;
  initial_balance: number; 
};

// This type defines what the FRONTEND SENDS to the backend for UPDATE
export type UpdateAccountPayload = {
  name?: string;
  initial_balance?: number; 
  current_balance?: number; 
};

