import React, { useMemo, useState } from "react";
import { createRoot } from "react-dom/client";
import {
  BadgeCheck,
  Banknote,
  FileUp,
  History,
  RefreshCcw,
  Search,
  SendHorizontal,
  ShieldCheck,
  Wallet
} from "lucide-react";
import "./styles.css";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

const initialTransfer = {
  senderUpiId: "9876543210@upi",
  receiverUpiId: "9123456780@upi",
  amount: "125.50"
};

const initialFunding = {
  upiId: "9876543210@upi",
  amount: "500.00",
  bankReferenceId: "BANK-REF-1001"
};

const initialKyc = {
  userEmail: "new.user@example.com",
  documentType: "NATIONAL_ID",
  documentNumber: "DOC-2001",
  mobileNumber: "9000000001"
};

function App() {
  const [activeTab, setActiveTab] = useState("wallet");
  const [lookupUpi, setLookupUpi] = useState("9876543210@upi");
  const [wallet, setWallet] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [funding, setFunding] = useState(initialFunding);
  const [transfer, setTransfer] = useState(initialTransfer);
  const [kyc, setKyc] = useState(initialKyc);
  const [kycFile, setKycFile] = useState(null);
  const [kycId, setKycId] = useState("");
  const [status, setStatus] = useState({
    type: "idle",
    message: "Backend API: http://localhost:8080"
  });

  const stats = useMemo(
    () => [
      {
        label: "Balance",
        value: wallet ? formatMoney(wallet.balance) : "Not loaded",
        icon: Wallet
      },
      {
        label: "UPI ID",
        value: wallet?.upiId || lookupUpi,
        icon: ShieldCheck
      },
      {
        label: "Ledger Items",
        value: transactions.length.toString(),
        icon: History
      }
    ],
    [wallet, lookupUpi, transactions.length]
  );

  async function request(path, options = {}) {
    const response = await fetch(`${API_BASE_URL}${path}`, options);
    const payload = await response.json();
    if (!response.ok) {
      throw new Error(payload.message || "Request failed");
    }
    return payload;
  }

  async function runAction(action, successMessage) {
    setStatus({ type: "loading", message: "Working..." });
    try {
      const result = await action();
      setStatus({ type: "success", message: successMessage || result.message });
      return result;
    } catch (error) {
      setStatus({ type: "error", message: error.message });
      return null;
    }
  }

  async function loadWallet() {
    const result = await runAction(async () => {
      const walletResult = await request(`/api/v1/wallet/${encodeURIComponent(lookupUpi)}`);
      const historyResult = await request(`/api/v1/wallet/${encodeURIComponent(lookupUpi)}/transactions`);
      setWallet(walletResult.data);
      setTransactions(historyResult.data || []);
      return walletResult;
    }, "Wallet loaded");
    return result;
  }

  async function fundWallet(event) {
    event.preventDefault();
    await runAction(async () => {
      const result = await request("/api/v1/wallet/fund", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...funding, amount: Number(funding.amount) })
      });
      setLookupUpi(result.data.upiId);
      setWallet(result.data);
      return result;
    }, "Wallet funded");
  }

  async function sendMoney(event) {
    event.preventDefault();
    await runAction(async () => {
      const result = await request("/api/v1/wallet/transfer", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...transfer, amount: Number(transfer.amount) })
      });
      if (transfer.senderUpiId === lookupUpi) {
        await loadWallet();
      }
      return result;
    }, "Transfer completed");
  }

  async function simulateFailure() {
    await runAction(async () => {
      const result = await request("/api/v1/wallet/simulate-failure", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...transfer, amount: Number(transfer.amount) })
      });
      if (transfer.senderUpiId === lookupUpi) {
        await loadWallet();
      }
      return result;
    }, "Failure simulation completed");
  }

  async function submitKyc(event) {
    event.preventDefault();
    await runAction(async () => {
      const formData = new FormData();
      Object.entries(kyc).forEach(([key, value]) => formData.append(key, value));
      formData.append("document", kycFile || new Blob(["sample kyc"], { type: "text/plain" }), "kyc.txt");
      const result = await request("/api/v1/wallet/kyc/upload", {
        method: "POST",
        body: formData
      });
      setKycId(result.data.id);
      return result;
    }, "KYC submitted");
  }

  async function approveKyc(event) {
    event.preventDefault();
    await runAction(async () => {
      const result = await request(`/api/v1/admin/kyc/${kycId}/approve`, {
        method: "POST"
      });
      setLookupUpi(result.data.upiId);
      setWallet(result.data);
      return result;
    }, "KYC approved");
  }

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark">
            <Wallet size={22} />
          </div>
          <div>
            <h1>Wallet Simulator</h1>
            <p>Spring Boot API console</p>
          </div>
        </div>
        <nav className="nav-list" aria-label="Primary">
          <TabButton icon={Wallet} id="wallet" label="Wallet" activeTab={activeTab} setActiveTab={setActiveTab} />
          <TabButton icon={FileUp} id="kyc" label="KYC" activeTab={activeTab} setActiveTab={setActiveTab} />
          <TabButton icon={SendHorizontal} id="transfer" label="Transfer" activeTab={activeTab} setActiveTab={setActiveTab} />
          <TabButton icon={History} id="history" label="History" activeTab={activeTab} setActiveTab={setActiveTab} />
        </nav>
      </aside>

      <section className="workspace">
        <header className="topbar">
          <div>
            <p className="eyebrow">Local API workspace</p>
            <h2>{sectionTitle(activeTab)}</h2>
          </div>
          <div className={`status ${status.type}`}>{status.message}</div>
        </header>

        <section className="stats-grid">
          {stats.map((stat) => (
            <article className="stat-card" key={stat.label}>
              <stat.icon size={20} />
              <span>{stat.label}</span>
              <strong>{stat.value}</strong>
            </article>
          ))}
        </section>

        {activeTab === "wallet" && (
          <section className="panel-grid">
            <div className="panel wide">
              <div className="panel-header">
                <h3>Wallet Lookup</h3>
                <button className="icon-button" type="button" onClick={loadWallet} title="Refresh wallet">
                  <RefreshCcw size={18} />
                </button>
              </div>
              <label>
                UPI ID
                <div className="inline-control">
                  <input value={lookupUpi} onChange={(event) => setLookupUpi(event.target.value)} />
                  <button type="button" onClick={loadWallet}>
                    <Search size={18} />
                    Load
                  </button>
                </div>
              </label>
            </div>

            <form className="panel" onSubmit={fundWallet}>
              <div className="panel-header">
                <h3>Fund Wallet</h3>
                <Banknote size={20} />
              </div>
              <Field label="UPI ID" value={funding.upiId} onChange={(value) => setFunding({ ...funding, upiId: value })} />
              <Field label="Amount" type="number" step="0.01" value={funding.amount} onChange={(value) => setFunding({ ...funding, amount: value })} />
              <Field label="Bank Reference" value={funding.bankReferenceId} onChange={(value) => setFunding({ ...funding, bankReferenceId: value })} />
              <button type="submit">
                <Banknote size={18} />
                Add Funds
              </button>
            </form>
          </section>
        )}

        {activeTab === "kyc" && (
          <section className="panel-grid">
            <form className="panel" onSubmit={submitKyc}>
              <div className="panel-header">
                <h3>Submit KYC</h3>
                <FileUp size={20} />
              </div>
              <Field label="Email" value={kyc.userEmail} onChange={(value) => setKyc({ ...kyc, userEmail: value })} />
              <Field label="Mobile Number" value={kyc.mobileNumber} onChange={(value) => setKyc({ ...kyc, mobileNumber: value })} />
              <label>
                Document Type
                <select value={kyc.documentType} onChange={(event) => setKyc({ ...kyc, documentType: event.target.value })}>
                  <option value="NATIONAL_ID">National ID</option>
                  <option value="PASSPORT">Passport</option>
                  <option value="DRIVERS_LICENSE">Driver License</option>
                  <option value="VOTER_ID">Voter ID</option>
                </select>
              </label>
              <Field label="Document Number" value={kyc.documentNumber} onChange={(value) => setKyc({ ...kyc, documentNumber: value })} />
              <label>
                Document File
                <input type="file" onChange={(event) => setKycFile(event.target.files?.[0] || null)} />
              </label>
              <button type="submit">
                <FileUp size={18} />
                Submit KYC
              </button>
            </form>

            <form className="panel" onSubmit={approveKyc}>
              <div className="panel-header">
                <h3>Admin Approval</h3>
                <BadgeCheck size={20} />
              </div>
              <Field label="KYC ID" value={kycId} onChange={setKycId} />
              <button type="submit">
                <BadgeCheck size={18} />
                Approve
              </button>
            </form>
          </section>
        )}

        {activeTab === "transfer" && (
          <section className="panel-grid">
            <form className="panel" onSubmit={sendMoney}>
              <div className="panel-header">
                <h3>Send Money</h3>
                <SendHorizontal size={20} />
              </div>
              <Field label="Sender UPI" value={transfer.senderUpiId} onChange={(value) => setTransfer({ ...transfer, senderUpiId: value })} />
              <Field label="Receiver UPI" value={transfer.receiverUpiId} onChange={(value) => setTransfer({ ...transfer, receiverUpiId: value })} />
              <Field label="Amount" type="number" step="0.01" value={transfer.amount} onChange={(value) => setTransfer({ ...transfer, amount: value })} />
              <button type="submit">
                <SendHorizontal size={18} />
                Transfer
              </button>
              <button className="secondary" type="button" onClick={simulateFailure}>
                <RefreshCcw size={18} />
                Simulate Refund
              </button>
            </form>
          </section>
        )}

        {activeTab === "history" && (
          <section className="panel wide">
            <div className="panel-header">
              <h3>Recent Transactions</h3>
              <button className="icon-button" type="button" onClick={loadWallet} title="Refresh history">
                <RefreshCcw size={18} />
              </button>
            </div>
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Status</th>
                    <th>Amount</th>
                    <th>Remarks</th>
                  </tr>
                </thead>
                <tbody>
                  {transactions.length === 0 ? (
                    <tr>
                      <td colSpan="4" className="empty">Load a wallet to view ledger entries.</td>
                    </tr>
                  ) : (
                    transactions.map((transaction) => (
                      <tr key={transaction.id}>
                        <td>{transaction.id}</td>
                        <td><span className="pill">{transaction.status}</span></td>
                        <td>{formatMoney(transaction.amount)}</td>
                        <td>{transaction.remarks}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </section>
        )}
      </section>
    </main>
  );
}

function TabButton({ icon: Icon, id, label, activeTab, setActiveTab }) {
  return (
    <button className={activeTab === id ? "active" : ""} type="button" onClick={() => setActiveTab(id)}>
      <Icon size={18} />
      {label}
    </button>
  );
}

function Field({ label, value, onChange, type = "text", step }) {
  return (
    <label>
      {label}
      <input type={type} step={step} value={value} onChange={(event) => onChange(event.target.value)} />
    </label>
  );
}

function sectionTitle(activeTab) {
  const titles = {
    wallet: "Wallet Dashboard",
    kyc: "KYC Operations",
    transfer: "Transfer Console",
    history: "Transaction History"
  };
  return titles[activeTab];
}

function formatMoney(value) {
  const amount = Number(value || 0);
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR"
  }).format(amount);
}

createRoot(document.getElementById("root")).render(<App />);
