
import React, { ReactNode, useState } from 'react';
import { Link } from 'react-router-dom';
import { User } from './types';

// Icon Components (Heroicons)
export const UserIcon: React.FC<{ className?: string }> = ({ className = "w-6 h-6" }) => (
  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className={className}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 6a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0ZM4.501 20.118a7.5 7.5 0 0 1 14.998 0A18.75 18.75 0 0 1 12 22.5c-2.786 0-5.433-.608-7.499-1.682Z" />
  </svg>
);

export const CogIcon: React.FC<{ className?: string }> = ({ className = "w-6 h-6" }) => (
  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className={className}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M4.5 12a7.5 7.5 0 0 0 15 0m-15 0a7.5 7.5 0 1 1 15 0m-15 0H3m18 0h-1.5m-15 0H3.75m16.5 0c.071 0 .142.002.21.006M12 2.25a8.975 8.975 0 0 0-2.036.335A7.46 7.46 0 0 1 12 2.25Zm0 0a8.975 8.975 0 0 1 2.036.335A7.46 7.46 0 0 0 12 2.25Zm0 19.5a8.975 8.975 0 0 0 2.036-.335A7.46 7.46 0 0 1 12 21.75Zm0 0a8.975 8.975 0 0 1-2.036-.335A7.46 7.46 0 0 0 12 21.75Zm-9.75-6.375A7.46 7.46 0 0 1 2.25 12Zm0 0a7.46 7.46 0 0 0-.036 2.036A8.975 8.975 0 0 1 2.25 12Zm19.5 0a7.46 7.46 0 0 0 .036 2.036A8.975 8.975 0 0 0 21.75 12Zm0 0a7.46 7.46 0 0 1 .036-2.036A8.975 8.975 0 0 1 21.75 12Z" />
  </svg>
);

export const LogoutIcon: React.FC<{ className?: string }> = ({ className = "w-6 h-6" }) => (
  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className={className}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M15.75 9V5.25A2.25 2.25 0 0 0 13.5 3h-6a2.25 2.25 0 0 0-2.25 2.25v13.5A2.25 2.25 0 0 0 7.5 21h6a2.25 2.25 0 0 0 2.25-2.25V15m3 0 3-3m0 0-3-3m3 3H9" />
  </svg>
);

export const BellIcon: React.FC<{ className?: string }> = ({ className = "w-6 h-6" }) => (
  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className={className}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M14.857 17.082a23.848 23.848 0 0 0 5.454-1.31A8.967 8.967 0 0 1 18 9.75V9A6 6 0 0 0 6 9v.75a8.967 8.967 0 0 1-2.312 6.022c1.733.64 3.56 1.017 5.454 1.31m5.714 0a24.255 24.255 0 0 1-5.714 0m5.714 0a3 3 0 1 1-5.714 0M3.124 7.5A8.969 8.969 0 0 1 5.292 3m13.416 0a8.969 8.969 0 0 1 2.168 4.5" />
  </svg>
);

export const UsersIcon: React.FC<{ className?: string }> = ({ className = "w-6 h-6" }) => (
  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className={className}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M18 18.72a9.094 9.094 0 0 0 3.741-.479 3 3 0 0 0-3.741-5.588M14.25 10.5a8.25 8.25 0 1 0 0-7.5 8.25 8.25 0 0 0 0 7.5ZM10.5 0.075c3.452 0 6.574 2.118 7.688 5.162M10.5 0.075A2.25 2.25 0 0 0 8.25 2.25v9.75c0 .927.424 1.769 1.092 2.339M10.5 0.075c-3.452 0-6.574 2.118-7.688 5.162m0 0A2.25 2.25 0 0 1 3 7.5v9.75c0 .927.424 1.769 1.092 2.339m9.158-8.127a2.25 2.25 0 0 1-2.25-2.25V5.162m0 0A2.25 2.25 0 0 0 8.25 3v2.162m0 0A2.25 2.25 0 0 0 6 7.5v2.25m0 0a2.25 2.25 0 0 0 2.25 2.25m0 0a2.25 2.25 0 0 1 2.25-2.25m0 0A2.25 2.25 0 0 1 12.75 12H15m2.25-2.25a2.25 2.25 0 0 1 2.25 2.25m0 0a2.25 2.25 0 0 0-2.25 2.25m-15-2.25a2.25 2.25 0 0 1-2.25-2.25m0 0A2.25 2.25 0 0 0 3 7.5m0 0V5.162m0 0A2.25 2.25 0 0 1 5.25 3M3 12a2.25 2.25 0 0 0-2.25 2.25m0 0a2.25 2.25 0 0 1 2.25-2.25" />
  </svg>
);

export const ChatBubbleIcon: React.FC<{ className?: string }> = ({ className = "w-6 h-6" }) => (
  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className={className}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M20.25 8.511c.884.284 1.5 1.128 1.5 2.097v4.286c0 1.136-.847 2.1-1.98 2.193-.34.027-.68.052-1.02.072v3.091l-3.682-3.091c-.326-.275-.724-.386-1.118-.386H7.625c-1.136 0-2.097-.847-2.193-1.98A18.75 18.75 0 0 1 5.25 12.511V8.227c0-1.136.847-2.1 1.98-2.193.34-.027.68-.052 1.02-.072V3.75L12 6.841c.325.275.724.386 1.118.386h3.382c.884 0 1.67.284 2.25.779Z" />
  </svg>
);

export const VideoCameraIcon: React.FC<{ className?: string }> = ({ className = "w-6 h-6" }) => (
  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className={className}>
    <path strokeLinecap="round" strokeLinejoin="round" d="m15.75 10.5 4.72-4.72a.75.75 0 0 1 1.28.53v11.38a.75.75 0 0 1-1.28.53l-4.72-4.72M4.5 18.75h9a2.25 2.25 0 0 0 2.25-2.25v-9a2.25 2.25 0 0 0-2.25-2.25h-9A2.25 2.25 0 0 0 2.25 7.5v9A2.25 2.25 0 0 0 4.5 18.75Z" />
  </svg>
);

export const CalendarDaysIcon: React.FC<{ className?: string }> = ({ className = "w-6 h-6" }) => (
  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className={className}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 0 1 2.25-2.25h13.5A2.25 2.25 0 0 1 21 7.5v11.25m-18 0A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75m-18 0v-7.5A2.25 2.25 0 0 1 5.25 9h13.5A2.25 2.25 0 0 1 21 11.25v7.5m-9-3.75h.008v.008H12v-.008ZM12 15h.008v.008H12V15Zm0 2.25h.008v.008H12v-.008ZM9.75 15h.008v.008H9.75V15Zm0 2.25h.008v.008H9.75v-.008ZM7.5 15h.008v.008H7.5V15Zm0 2.25h.008v.008H7.5v-.008Zm6.75-4.5h.008v.008h-.008v-.008Zm0 2.25h.008v.008h-.008V15Zm0 2.25h.008v.008h-.008v-.008Zm2.25-4.5h.008v.008H16.5v-.008Zm0 2.25h.008v.008H16.5V15Z" />
  </svg>
);

export const PlusCircleIcon: React.FC<{ className?: string }> = ({ className = "w-6 h-6" }) => (
  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className={className}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v6m3-3H9m12 0a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
  </svg>
);

export const TrashIcon: React.FC<{ className?: string }> = ({ className = "w-6 h-6" }) => (
  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className={className}>
    <path strokeLinecap="round" strokeLinejoin="round" d="m14.74 9-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 0 1-2.244 2.077H8.084a2.25 2.25 0 0 1-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 0 0-3.478-.397m-12.56 0c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 0 1 3.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 0 0-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 0 0-7.5 0" />
  </svg>
);
// Renaming to avoid conflict if Heroicons' TrashIcon is also imported directly in App.tsx
export { TrashIcon as ComponentTrashIcon };


export const XCircleIcon: React.FC<{ className?: string }> = ({ className = "w-6 h-6" }) => (
 <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className={className}>
  <path strokeLinecap="round" strokeLinejoin="round" d="m9.75 9.75 4.5 4.5m0-4.5-4.5 4.5M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
</svg>
);

export const XMarkIcon: React.FC<{ className?: string }> = ({ className = "w-6 h-6" }) => (
  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className={className}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M6 18 18 6M6 6l12 12" />
  </svg>
);


export const ArrowLeftIcon: React.FC<{ className?: string }> = ({ className = "w-6 h-6" }) => (
  <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className={className}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M10.5 19.5 3 12m0 0 7.5-7.5M3 12h18" />
  </svg>
);


// Button Component
interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost' | 'outline';
  size?: 'sm' | 'md' | 'lg';
  leftIcon?: ReactNode;
  rightIcon?: ReactNode;
}

export const Button: React.FC<ButtonProps> = ({
  children,
  variant = 'primary',
  size = 'md',
  className = '',
  leftIcon,
  rightIcon,
  ...props
}) => {
  const baseStyles = 'font-semibold rounded-lg focus:outline-none focus:ring-2 focus:ring-offset-2 transition-colors duration-150 flex items-center justify-center';
  
  const sizeStyles = {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2 text-base',
    lg: 'px-6 py-3 text-lg',
  };

  const variantStyles = {
    primary: 'bg-primary text-white hover:bg-primary-dark focus:ring-primary',
    secondary: 'bg-secondary text-neutral-800 hover:bg-secondary-dark focus:ring-secondary',
    danger: 'bg-red-500 text-white hover:bg-red-600 focus:ring-red-500',
    ghost: 'text-primary hover:bg-primary-light/20 focus:ring-primary',
    outline: 'border border-primary text-primary hover:bg-primary-light/20 focus:ring-primary'
  };

  return (
    <button
      className={`${baseStyles} ${sizeStyles[size]} ${variantStyles[variant]} ${className}`}
      {...props}
    >
      {leftIcon && <span className="mr-2">{leftIcon}</span>}
      {children}
      {rightIcon && <span className="ml-2">{rightIcon}</span>}
    </button>
  );
};

// Input Component
interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  Icon?: React.ElementType;
}

export const Input: React.FC<InputProps> = ({ label, name, error, Icon, type="text", className, ...props }) => {
  return (
    <div className="w-full">
      {label && <label htmlFor={name} className="block text-sm font-medium text-neutral-700 mb-1">{label}</label>}
      <div className="relative">
        {Icon && <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none"><Icon className="h-5 w-5 text-neutral-400" /></div>}
        <input
          id={name}
          name={name}
          type={type}
          className={`block w-full px-3 py-2 border rounded-md shadow-sm placeholder-neutral-400 focus:outline-none sm:text-sm ${Icon ? 'pl-10' : ''} ${error ? 'border-red-500 focus:ring-red-500 focus:border-red-500' : 'border-neutral-300 focus:ring-primary focus:border-primary'} ${className}`}
          {...props}
        />
      </div>
      {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
    </div>
  );
};

// TextArea Component
interface TextAreaProps extends React.TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string;
  error?: string;
}
export const TextArea: React.FC<TextAreaProps> = ({ label, name, error, className, ...props }) => {
  return (
    <div className="w-full">
      {label && <label htmlFor={name} className="block text-sm font-medium text-neutral-700 mb-1">{label}</label>}
      <textarea
        id={name}
        name={name}
        rows={3}
        className={`block w-full px-3 py-2 border rounded-md shadow-sm placeholder-neutral-400 focus:outline-none sm:text-sm ${error ? 'border-red-500 focus:ring-red-500 focus:border-red-500' : 'border-neutral-300 focus:ring-primary focus:border-primary'} ${className}`}
        {...props}
      />
      {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
    </div>
  );
};


// Modal Component
interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: ReactNode;
  footer?: ReactNode;
  size?: 'sm' | 'md' | 'lg' | 'xl';
}

export const Modal: React.FC<ModalProps> = ({ isOpen, onClose, title, children, footer, size = 'md' }) => {
  if (!isOpen) return null;

  const sizeClasses = {
    sm: 'max-w-sm',
    md: 'max-w-md',
    lg: 'max-w-lg',
    xl: 'max-w-xl',
  };

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
      <div className={`bg-white rounded-lg shadow-xl w-full ${sizeClasses[size]}`}>
        <div className="flex justify-between items-center p-4 sm:p-5 border-b border-neutral-200">
          <h3 className="text-lg sm:text-xl font-semibold text-neutral-800">{title}</h3>
          <button onClick={onClose} className="text-neutral-500 hover:text-neutral-700">
            <XMarkIcon className="w-6 h-6 sm:w-7 sm:h-7" />
          </button>
        </div>
        <div className="p-4 sm:p-5 text-neutral-600">{children}</div>
        {footer && <div className="flex justify-end space-x-3 p-4 sm:p-5 border-t border-neutral-200">{footer}</div>}
      </div>
    </div>
  );
};


// Card Component
interface CardProps {
  children: ReactNode;
  className?: string;
  title?: string;
  actions?: ReactNode;
}

export const Card: React.FC<CardProps> = ({ children, className, title, actions }) => {
  return (
    <div className={`bg-white shadow-lg rounded-xl p-6 ${className}`}>
      {(title || actions) && (
        <div className="flex justify-between items-center mb-4 pb-4 border-b border-neutral-200">
          {title && <h2 className="text-xl font-semibold text-neutral-800">{title}</h2>}
          {actions && <div className="flex space-x-2">{actions}</div>}
        </div>
      )}
      {children}
    </div>
  );
};


// ProfileSummaryCard Component (Epic 2)
interface ProfileSummaryCardProps {
  user: User;
  className?: string;
}
export const ProfileSummaryCard: React.FC<ProfileSummaryCardProps> = ({ user, className }) => {
  return (
    <Card className={`text-center ${className}`}>
      <img 
        src={user.profilePictureUrl || `https://picsum.photos/seed/${user.id}/150/150`} 
        alt={user.name || 'Profile'} 
        className="w-24 h-24 rounded-full mx-auto mb-4 border-2 border-primary"
      />
      <h3 className="text-lg font-semibold text-neutral-800">{user.name || '사용자 이름'}</h3>
      <p className="text-sm text-neutral-500 mb-1">{user.mbti || 'MBTI'}</p>
      {user.tags && user.tags.length > 0 && (
        <div className="mt-2 flex flex-wrap justify-center gap-2">
          {user.tags.slice(0,3).map(tag => (
            <span key={tag} className="px-2 py-1 bg-primary-light/20 text-primary text-xs rounded-full">{tag}</span>
          ))}
        </div>
      )}
      <Link to={`/users/${user.id}`}>
        <Button variant="outline" size="sm" className="mt-4 w-full">프로필 보기</Button>
      </Link>
    </Card>
  );
};

// Generic ItemList for selection (used in Team Formation)
interface ItemListSelectorProps<T> {
  items: T[];
  selectedItems: T[];
  onSelectItem: (item: T) => void;
  renderItem: (item: T, isSelected: boolean) => ReactNode;
  itemKey: keyof T;
  maxHeight?: string;
}

export const ItemListSelector = <T extends Record<string, any>>({ items, selectedItems, onSelectItem, renderItem, itemKey, maxHeight = 'max-h-60' }: ItemListSelectorProps<T>) => {
  return (
    <div className={`space-y-2 ${maxHeight} overflow-y-auto p-2 border border-neutral-300 rounded-md`}>
      {items.map(item => {
        const isSelected = selectedItems.some(selItem => selItem[itemKey] === item[itemKey]);
        return (
          <div 
            key={item[itemKey] as string} 
            onClick={() => onSelectItem(item)}
            className={`p-2 rounded-md cursor-pointer transition-colors ${isSelected ? 'bg-primary-light text-primary-dark font-semibold' : 'hover:bg-neutral-100'}`}
          >
            {renderItem(item, isSelected)}
          </div>
        );
      })}
       {items.length === 0 && <p className="text-xs text-neutral-500 text-center py-2">선택 가능한 항목이 없습니다.</p>}
    </div>
  );
};
