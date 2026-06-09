import React, { useEffect, useMemo, useState } from "react";
import { createRoot } from "react-dom/client";
import {
  Activity,
  ArrowDownLeft,
  BadgeCheck,
  Banknote,
  CheckCircle2,
  Clock3,
  FileUp,
  History,
  Landmark,
  Loader2,
  RefreshCcw,
  Search,
  SendHorizontal,
  ShieldCheck,
  Sparkles,
  UploadCloud,
  Wallet
} from "lucide-react";
import "./styles.css";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
const DEMO_SENDER_UPI = "9876543210@upi";
const DEMO_RECEIVER_UPI = "9123456780@upi";

const initialTransfer = {
  senderUpiId: DEMO_SENDER_UPI,
  receiverUpiId: DEMO_RECEIVER_UPI,
  amount: "125.50"
};

const initialFunding = {
  upiId: DEMO_SENDER_UPI,
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
  const [activeTab, setActiveTab] = useState("overview");
  const [lookupUpi, setLookupUpi] = useState(DEMO_SENDER_UPI);
  const [wallet, setWallet] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [funding, setFunding] = useState(initialFunding);
  const [transfer, setTransfer] = useState(initialTransfer);
  const [kyc, setKyc] = useState(initialKyc);
  const [kycFile, setKycFile] = useState(null);
  const [kycId, setKycId] = useState("");
  const [busyAction, setBusyAction] = useState("");
  const [status, setStatus] = useState({
    type: "idle",
    message: "Ready for local demo"
  });

  const isBusy = Boolean(busyAction);

  const ledgerTotals = useMemo(() => {
    return transactions.reduce(
      (totals, transaction) => {
        const amount = Number(transaction.amount || 0);
        if (transaction.status === "SUCCESS") {
          totals.success += 1;
          totals.volume += amount;
        }
        if (transaction.status === "REFUNDED") {
          totals.refunds += 1;
        }
        if (transaction.status === "FAILED") {
          totals.failed += 1;
        }
        return totals;
      },
      { success: 0, failed: 0, refunds: 0, volume: 0 }
    );
  }, [transactions]);

  const stats = useMemo(
    () => [
      {
        label: "Available Balance",
        value: wallet ? formatMoney(wallet.balance) : "Load wallet",
        detail: wallet ? wallet.upiId : DEMO_SENDER_UPI,
        icon: Wallet,
        tone: "mint"
      },
      {
        label: "Ledger Entries",
        value: transactions.length.toString(),
        detail: `${ledgerTotals.success} successful`,
        icon: History,
        tone: "blue"
      },
      {
        label: "Processed Volume",
        value: formatMoney(ledgerTotals.volume),
        detail: `${ledgerTotals.refunds} refunds tracked`,
        icon: Activity,
        tone: "gold"
      },
      {
        label: "API Status",
        value: status.type === "error" ? "Check API" : "Local",
        detail: API_BASE_URL,
        icon: ShieldCheck,
        tone: status.type === "error" ? "red" : "mint"
      }
    ],
    [ledgerTotals, status.type, transactions.length, wallet]
  );

  useEffect(() => {
    refreshWallet(DEMO_SENDER_UPI, { silent: true });
  }, []);

  async function request(path, options = {}) {
    const response = await fetch(`${API_BASE_URL}${path}`, options);
    const text = await response.text();
    const payload = text ? JSON.parse(text) : {};

    if (!response.ok) {
      throw new Error(payload.message || "Request failed");
    }

    return payload;
  }

  async function runAction(actionName, action, successMessage) {
    setBusyAction(actionName);
    setStatus({ type: "loading", message: "Working..." });

    try {
      const result = await action();
      setStatus({ type: "success", message: successMessage || result?.message || "Done" });
      return result;
    } catch (error) {
      setStatus({ type: "error", message: error.message || "Request failed" });
      return null;
    } finally {
      setBusyAction("");
    }
  }

  async function refreshWallet(upiId = lookupUpi, options = {}) {
    const walletResult = await request(`/api/v1/wallet/${encodeURIComponent(upiId)}`);
    const historyResult = await request(`/api/v1/wallet/${encodeURIComponent(upiId)}/transactions`);

    setLookupUpi(upiId);
    setWallet(walletResult.data);
    setTransactions(historyResult.data || []);

    if (!options.silent) {
      setStatus({ type: "success", message: "Wallet loaded" });
    }

    return walletResult;
  }

  async function loadWallet(event) {
    event?.preventDefault();
    return runAction("load", () => refreshWallet(lookupUpi, { silent: true }), "Wallet loaded");
  }

  async function loadDemoWallet() {
    return runAction("demo", () => refreshWallet(DEMO_SENDER_UPI, { silent: true }), "Demo wallet loaded");
  }

  async function fundWallet(event) {
    event.preventDefault();
    await runAction(
      "fund",
      async () => {
        const result = await request("/api/v1/wallet/fund", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ ...funding, amount: Number(funding.amount) })
        });
        await refreshWallet(result.data.upiId, { silent: true });
        return result;
      },
      "Wallet funded"
    );
  }

  async function sendMoney(event) {
    event.preventDefault();
    await runAction(
      "transfer",
      async () => {
        const result = await request("/api/v1/wallet/transfer", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ ...transfer, amount: Number(transfer.amount) })
        });
        await refreshWallet(transfer.senderUpiId, { silent: true });
        return result;
      },
      "Transfer completed"
    );
  }

  async function simulateFailure() {
    await runAction(
      "refund",
      async () => {
        const result = await request("/api/v1/wallet/simulate-failure", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ ...transfer, amount: Number(transfer.amount) })
        });
        await refreshWallet(transfer.senderUpiId, { silent: true });
        return result;
      },
      "Refund simulation completed"
    );
  }

  async function submitKyc(event) {
    event.preventDefault();
    await runAction(
      "kyc",
      async () => {
        const formData = new FormData();
        Object.entries(kyc).forEach(([key, value]) => formData.append(key, value));
        formData.append("document", kycFile || new Blob(["sample kyc"], { type: "text/plain" }), "kyc.txt");

        const result = await request("/api/v1/wallet/kyc/upload", {
          method: "POST",
          body: formData
        });
        setKycId(result.data.id);
        return result;
      },
      "KYC submitted"
    );
  }

  async function approveKyc(event) {
    event.preventDefault();
    await runAction(
      "approve",
      async () => {
        const result = await request(`/api/v1/admin/kyc/${kycId}/approve`, {
          method: "POST"
        });
        setLookupUpi(result.data.upiId);
        setWallet(result.data);
        setTransactions([]);
        return result;
      },
      "KYC approved"
    );
  }

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark">
            <Wallet size={22} />
          </div>
          <div>
            <h1>Wallet Studio</h1>
            <p>Spring Boot wallet simulator</p>
          </div>
        </div>

        <nav className="nav-list" aria-label="Primary">
          <TabButton icon={Sparkles} id="overview" label="Overview" activeTab={activeTab} setActiveTab={setActiveTab} />
          <TabButton icon={FileUp} id="kyc" label="KYC" activeTab={activeTab} setActiveTab={setActiveTab} />
          <TabButton icon={SendHorizontal} id="transfer" label="Transfer" activeTab={activeTab} setActiveTab={setActiveTab} />
          <TabButton icon={History} id="history" label="History" activeTab={activeTab} setActiveTab={setActiveTab} />
        </nav>

        <div className="sidebar-card">
          <span>Demo sender</span>
          <strong>{DEMO_SENDER_UPI}</strong>
          <button type="button" onClick={loadDemoWallet} disabled={isBusy}>
            <RefreshCcw size={16} />
            Load Demo
          </button>
        </div>
      </aside>

      <section className="workspace">
        <header className="topbar">
          <div>
            <p className="eyebrow">Local full-stack workspace</p>
            <h2>{sectionTitle(activeTab)}</h2>
          </div>
          <StatusBadge status={status} />
        </header>

        <section className="stats-grid">
          {stats.map((stat) => (
            <StatCard stat={stat} key={stat.label} />
          ))}
        </section>

        {activeTab === "overview" && (
          <section className="overview-grid">
            <WalletHero
              wallet={wallet}
              lookupUpi={lookupUpi}
              setLookupUpi={setLookupUpi}
              loadWallet={loadWallet}
              isBusy={isBusy}
              busyAction={busyAction}
            />
            <DemoFlow wallet={wallet} transactions={transactions} />
            <FundingPanel funding={funding} setFunding={setFunding} fundWallet={fundWallet} isBusy={isBusy} busyAction={busyAction} />
            <MiniLedger transactions={transactions} onViewHistory={() => setActiveTab("history")} />
          </section>
        )}

        {activeTab === "kyc" && (
          <section className="panel-grid">
            <form className="panel form-panel" onSubmit={submitKyc}>
              <PanelTitle icon={UploadCloud} title="Submit KYC" subtitle="Create a pending customer profile with a document upload." />
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
              <PrimaryButton type="submit" icon={FileUp} busy={busyAction === "kyc"} disabled={isBusy}>
                Submit KYC
              </PrimaryButton>
            </form>

            <form className="panel approval-panel" onSubmit={approveKyc}>
              <PanelTitle icon={BadgeCheck} title="Admin Approval" subtitle="Approve the submitted KYC ID and mint a new wallet." />
              <div className="approval-preview">
                <span>KYC ID</span>
                <strong>{kycId || "Waiting for submission"}</strong>
              </div>
              <Field label="KYC ID" value={kycId} onChange={setKycId} />
              <PrimaryButton type="submit" icon={BadgeCheck} busy={busyAction === "approve"} disabled={isBusy || !kycId}>
                Approve KYC
              </PrimaryButton>
            </form>
          </section>
        )}

        {activeTab === "transfer" && (
          <section className="transfer-layout">
            <form className="panel transfer-panel" onSubmit={sendMoney}>
              <PanelTitle icon={SendHorizontal} title="Send Money" subtitle="Move funds between two active demo wallets." />
              <Field label="Sender UPI" value={transfer.senderUpiId} onChange={(value) => setTransfer({ ...transfer, senderUpiId: value })} />
              <Field label="Receiver UPI" value={transfer.receiverUpiId} onChange={(value) => setTransfer({ ...transfer, receiverUpiId: value })} />
              <Field label="Amount" type="number" step="0.01" value={transfer.amount} onChange={(value) => setTransfer({ ...transfer, amount: value })} />
              <div className="button-row">
                <PrimaryButton type="submit" icon={SendHorizontal} busy={busyAction === "transfer"} disabled={isBusy}>
                  Transfer
                </PrimaryButton>
                <button className="secondary-button" type="button" onClick={simulateFailure} disabled={isBusy}>
                  {busyAction === "refund" ? <Loader2 className="spin" size={18} /> : <RefreshCcw size={18} />}
                  Simulate Refund
                </button>
              </div>
            </form>

            <div className="panel payment-preview">
              <PanelTitle icon={Landmark} title="Payment Rail" subtitle="A compact view of the transfer path." />
              <div className="rail-card sender">
                <span>Sender</span>
                <strong>{transfer.senderUpiId}</strong>
              </div>
              <div className="rail-line">
                <span>{formatMoney(transfer.amount)}</span>
              </div>
              <div className="rail-card receiver">
                <span>Receiver</span>
                <strong>{transfer.receiverUpiId}</strong>
              </div>
            </div>
          </section>
        )}

        {activeTab === "history" && (
          <section className="history-layout">
            <div className="panel wide">
              <div className="panel-header">
                <PanelTitle icon={History} title="Recent Transactions" subtitle="The latest ledger entries for the loaded wallet." />
                <button className="icon-button" type="button" onClick={loadWallet} title="Refresh history" disabled={isBusy}>
                  <RefreshCcw size={18} />
                </button>
              </div>
              <TransactionTable transactions={transactions} />
            </div>
          </section>
        )}
      </section>
    </main>
  );
}

function WalletHero({ wallet, lookupUpi, setLookupUpi, loadWallet, isBusy, busyAction }) {
  return (
    <section className="wallet-hero">
      <div className="wallet-card">
        <div className="wallet-card-top">
          <span>Active wallet</span>
          <ShieldCheck size={22} />
        </div>
        <strong>{wallet ? formatMoney(wallet.balance) : "Load wallet"}</strong>
        <p>{wallet?.upiId || lookupUpi}</p>
        <div className="wallet-card-footer">
          <span>{wallet?.userEmail || "Demo profile"}</span>
          <span>{wallet?.mobileNumber || "9876543210"}</span>
        </div>
      </div>

      <form className="lookup-card" onSubmit={loadWallet}>
        <PanelTitle icon={Search} title="Wallet Lookup" subtitle="Load a seeded dev wallet or any approved UPI ID." />
        <label>
          UPI ID
          <div className="inline-control">
            <input value={lookupUpi} onChange={(event) => setLookupUpi(event.target.value)} />
            <PrimaryButton type="submit" icon={Search} busy={busyAction === "load"} disabled={isBusy}>
              Load
            </PrimaryButton>
          </div>
        </label>
      </form>
    </section>
  );
}

function FundingPanel({ funding, setFunding, fundWallet, isBusy, busyAction }) {
  return (
    <form className="panel form-panel" onSubmit={fundWallet}>
      <PanelTitle icon={Banknote} title="Fund Wallet" subtitle="Simulate a bank deposit into the local wallet ledger." />
      <Field label="UPI ID" value={funding.upiId} onChange={(value) => setFunding({ ...funding, upiId: value })} />
      <Field label="Amount" type="number" step="0.01" value={funding.amount} onChange={(value) => setFunding({ ...funding, amount: value })} />
      <Field label="Bank Reference" value={funding.bankReferenceId} onChange={(value) => setFunding({ ...funding, bankReferenceId: value })} />
      <PrimaryButton type="submit" icon={Banknote} busy={busyAction === "fund"} disabled={isBusy}>
        Add Funds
      </PrimaryButton>
    </form>
  );
}

function DemoFlow({ wallet, transactions }) {
  const steps = [
    { label: "Backend online", done: Boolean(wallet), detail: "Dev profile seeds demo wallets" },
    { label: "Fund sender", done: Number(wallet?.balance || 0) > 0 || transactions.length > 0, detail: "Creates bank funding ledger entry" },
    { label: "Transfer money", done: transactions.some((tx) => tx.remarks?.toLowerCase().includes("transfer")), detail: "Updates balances and transaction history" }
  ];

  return (
    <section className="panel flow-panel">
      <PanelTitle icon={CheckCircle2} title="Demo Flow" subtitle="A quick path for reviewing the full-stack behavior." />
      <div className="flow-list">
        {steps.map((step, index) => (
          <div className={`flow-step ${step.done ? "done" : ""}`} key={step.label}>
            <span>{step.done ? <CheckCircle2 size={17} /> : index + 1}</span>
            <div>
              <strong>{step.label}</strong>
              <p>{step.detail}</p>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}

function MiniLedger({ transactions, onViewHistory }) {
  const recent = transactions.slice(0, 3);

  return (
    <section className="panel mini-ledger">
      <div className="panel-header">
        <PanelTitle icon={History} title="Activity" subtitle="Latest movements for the loaded wallet." />
        <button className="text-button" type="button" onClick={onViewHistory}>View all</button>
      </div>
      {recent.length === 0 ? (
        <EmptyState title="No activity yet" text="Fund the wallet or send money to create ledger entries." />
      ) : (
        <div className="activity-list">
          {recent.map((transaction) => (
            <ActivityItem transaction={transaction} key={transaction.id} />
          ))}
        </div>
      )}
    </section>
  );
}

function TransactionTable({ transactions }) {
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Status</th>
            <th>Amount</th>
            <th>Timestamp</th>
            <th>Remarks</th>
          </tr>
        </thead>
        <tbody>
          {transactions.length === 0 ? (
            <tr>
              <td colSpan="5" className="empty">Load a wallet, fund it, or send money to view ledger entries.</td>
            </tr>
          ) : (
            transactions.map((transaction) => (
              <tr key={transaction.id}>
                <td>#{transaction.id}</td>
                <td><StatusPill status={transaction.status} /></td>
                <td>{formatMoney(transaction.amount)}</td>
                <td>{formatDate(transaction.timestamp)}</td>
                <td>{transaction.remarks}</td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}

function ActivityItem({ transaction }) {
  return (
    <article className="activity-item">
      <div className={`activity-icon ${transaction.status?.toLowerCase()}`}>
        {transaction.status === "REFUNDED" ? <ArrowDownLeft size={17} /> : <Banknote size={17} />}
      </div>
      <div>
        <strong>{transaction.remarks || "Ledger entry"}</strong>
        <span>{formatDate(transaction.timestamp)}</span>
      </div>
      <b>{formatMoney(transaction.amount)}</b>
    </article>
  );
}

function StatCard({ stat }) {
  return (
    <article className={`stat-card ${stat.tone}`}>
      <div className="stat-icon">
        <stat.icon size={20} />
      </div>
      <span>{stat.label}</span>
      <strong>{stat.value}</strong>
      <p>{stat.detail}</p>
    </article>
  );
}

function PanelTitle({ icon: Icon, title, subtitle }) {
  return (
    <div className="title-block">
      <div className="title-icon">
        <Icon size={18} />
      </div>
      <div>
        <h3>{title}</h3>
        <p>{subtitle}</p>
      </div>
    </div>
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

function PrimaryButton({ children, icon: Icon, busy, disabled, type = "button" }) {
  return (
    <button className="primary-button" type={type} disabled={disabled}>
      {busy ? <Loader2 className="spin" size={18} /> : <Icon size={18} />}
      {children}
    </button>
  );
}

function StatusBadge({ status }) {
  const icon = status.type === "loading" ? Loader2 : status.type === "error" ? Clock3 : CheckCircle2;
  const Icon = icon;
  return (
    <div className={`status ${status.type}`}>
      <Icon className={status.type === "loading" ? "spin" : ""} size={17} />
      {status.message}
    </div>
  );
}

function StatusPill({ status }) {
  return <span className={`pill ${status?.toLowerCase()}`}>{status}</span>;
}

function EmptyState({ title, text }) {
  return (
    <div className="empty-state">
      <History size={24} />
      <strong>{title}</strong>
      <p>{text}</p>
    </div>
  );
}

function sectionTitle(activeTab) {
  const titles = {
    overview: "Wallet Dashboard",
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

function formatDate(value) {
  if (!value) {
    return "Just now";
  }

  return new Intl.DateTimeFormat("en-IN", {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit"
  }).format(new Date(value));
}

createRoot(document.getElementById("root")).render(<App />);
